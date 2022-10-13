package de.slevermann.fabric.coordreminder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import de.slevermann.fabric.coordreminder.command.ClearCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.DeleteCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.GetCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.ListCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.NameSuggestionProvider;
import de.slevermann.fabric.coordreminder.command.PromoteCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.RenameCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.SetCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.ShareCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.TeleportCoordinateCommand;
import de.slevermann.fabric.coordreminder.db.ConnectionPool;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import de.slevermann.fabric.coordreminder.db.DbCoordinate;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class CoordReminder implements DedicatedServerModInitializer {

    public static final UUID GLOBAL_COORDINATE_UUID = UUID.fromString("4c6b776e-1aa4-448b-a507-12c70461ff03");

    private static final Logger LOGGER = LoggerFactory.getLogger(CoordReminder.class);

    private static final Type TYPE = new TypeToken<Map<UUID, Map<String, Coordinate>>>() {
    }.getType();

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Coordinate.class, new CoordinateDeserializer())
            .create();

    private CoordinateService dbService;

    private Configuration configSetup() {
        final var configFile = Paths.get("./coordreminder-config.json");
        if (Files.notExists(configFile)) {
            writeExampleConfigs();
            throw new RuntimeException("Coordreminder needs configuration. Please review example files " +
                                       "'coordreminder-h2-config.json' and 'coordreminder-mariadb-config.json', create " +
                                       "'coordreminder-config.json' and configure as appropriate");
        }
        try (final var is = Files.newInputStream(configFile);
             final var isr = new InputStreamReader(is)) {
            return GSON.fromJson(isr, Configuration.class);
        } catch (final IOException e) {
            LOGGER.error("Failed to read configuration file 'coordreminder-config.json'");
            throw new RuntimeException(e);
        } catch (final JsonParseException e) {
            LOGGER.error("Failed to read configuration file 'coordreminder-config.json'");
            throw e;
        }
    }

    private void writeExampleConfigs() {
        final var h2Config = Paths.get("./coordreminder-h2-config.json");
        final var mariadbConfig = Paths.get("./coordreminder-mariadb-config.json");

        if (Files.notExists(h2Config) || Files.notExists(mariadbConfig)) {
            try (final var h2Os = Files.newOutputStream(h2Config);
                 final var mariaOs = Files.newOutputStream(mariadbConfig);
                 final var h2Is = getClass().getResourceAsStream("/coordreminder-h2-config.json");
                 final var mariaIs = getClass().getResourceAsStream("/coordreminder-mariadb-config.json")) {
                h2Is.transferTo(h2Os);
                mariaIs.transferTo(mariaOs);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void importLegacyCoordinates(final CoordinateService coordinateService) {
        final var coordFile = Paths.get("coordreminder/coordinates.json");
        if (Files.exists(coordFile)) {
            try (final var is = Files.newInputStream(coordFile);
                 final var reader = new InputStreamReader(is)) {
                final Map<UUID, Map<String, Coordinate>> coords = GSON.fromJson(reader, TYPE);
                var failed = false;
                for (final var entry : coords.entrySet()) {
                    final var owner = entry.getKey();
                    for (final var coordinateEntry : entry.getValue().entrySet()) {
                        final var coordinate = coordinateEntry.getValue();
                        final var name = coordinateEntry.getKey();
                        final var dbCoordinate = new DbCoordinate(name, "minecraft:dimension",
                                coordinate.getDimension(), owner,
                                coordinate.getX(), coordinate.getY(), coordinate.getZ());
                        if (!coordinateService.createCoordinate(dbCoordinate)) {
                            failed = true;
                        }
                    }
                }
                if (failed) {
                    LOGGER.warn("Some coordinates failed to be important because they already existed in the db.");
                }
                LOGGER.info("Coordinate import complete, moving coordinate JSON to backup location" +
                            " 'coordreminder/coordinates-imported.json'");
                try {
                    Files.move(coordFile, Paths.get("coordreminder/coordinates-imported.json"), REPLACE_EXISTING);
                } catch (final IOException e) {
                    LOGGER.error("Failed to move coordinate json to backup location", e);
                }
            } catch (final IOException e) {
                LOGGER.error("Failed to import legacy coordinates from file");
            } catch (final SQLException e) {
                LOGGER.error("Failed to import legacy coordinates into database", e);
            }
        }
    }

    @Override
    public void onInitializeServer() {
        LOGGER.info("Launching CoordReminder");
        final var configuration = configSetup();
        final var connectionPool = new ConnectionPool(configuration);
        dbService = new CoordinateService(connectionPool);
        importLegacyCoordinates(dbService);

        final var coordNode = CommandManager.literal("coord").build();

        final var setNode = CommandManager.literal("set").build();
        coordNode.addChild(setNode);
        setNode.addChild(getNameArgNode(new SetCoordinateCommand(dbService, false), false, false));

        final var getNode = CommandManager.literal("get").build();
        coordNode.addChild(getNode);
        getNode.addChild(getNameArgNode(new GetCoordinateCommand(dbService, false), false, true));

        final var deleteNode = CommandManager.literal("delete").build();
        coordNode.addChild(deleteNode);
        deleteNode.addChild(getNameArgNode(new DeleteCoordinateCommand(dbService, false), false, true));

        final var teleportNode = CommandManager.literal("tp").build();
        coordNode.addChild(teleportNode);
        teleportNode.addChild(getNameArgNode(new TeleportCoordinateCommand(dbService, false), false, true));

        final var listNode = CommandManager.literal("list")
                .executes(new ListCoordinateCommand(dbService, false)).build();
        coordNode.addChild(listNode);

        final var shareNode = CommandManager.literal("share").build();
        coordNode.addChild(shareNode);
        shareNode.addChild(getNameArgNode(new ShareCoordinateCommand(dbService, false), false, true));

        final var clearNode = CommandManager.literal("clear")
                .executes(new ClearCoordinateCommand(dbService, false)).build();
        coordNode.addChild(clearNode);

        final var renameNode = CommandManager.literal("rename").build();
        coordNode.addChild(renameNode);
        renameNode.addChild(getNewNameNode(new RenameCoordinateCommand(dbService, false), false));

        final var promoteNode = CommandManager.literal("promote").build();
        coordNode.addChild(promoteNode);
        promoteNode.addChild(getNewNameNode(new PromoteCoordinateCommand(dbService), false));

        // Setup Global commands
        final var globalNode = CommandManager.literal("global").build();
        coordNode.addChild(globalNode);

        final var globalSetNode = CommandManager.literal("set").build();
        globalNode.addChild(globalSetNode);
        globalSetNode.addChild(getNameArgNode(new SetCoordinateCommand(dbService, true), true, false));

        final var globalGetNode = CommandManager.literal("get").build();
        globalNode.addChild(globalGetNode);
        globalGetNode.addChild(getNameArgNode(new GetCoordinateCommand(dbService, true), true, true));

        final var globalDeleteNode = CommandManager.literal("delete").build();
        globalNode.addChild(globalDeleteNode);
        globalDeleteNode.addChild(getNameArgNode(new DeleteCoordinateCommand(dbService, true), true, true));

        final var globalTeleportNode = CommandManager.literal("tp").build();
        globalNode.addChild(globalTeleportNode);
        globalTeleportNode.addChild(getNameArgNode(new TeleportCoordinateCommand(dbService, true), true, true));

        final var globalListNode = CommandManager.literal("list")
                .executes(new ListCoordinateCommand(dbService, true)).build();
        globalNode.addChild(globalListNode);

        final var globalShareNode = CommandManager.literal("share").build();
        globalNode.addChild(globalShareNode);
        globalShareNode.addChild(getNameArgNode(new ShareCoordinateCommand(dbService, true), true, true));

        final var globalClearNode = CommandManager.literal("clear")
                .executes(new ClearCoordinateCommand(dbService, true)).build();
        globalNode.addChild(globalClearNode);

        final var globalRenameNode = CommandManager.literal("rename").build();
        globalNode.addChild(globalRenameNode);
        globalRenameNode.addChild(getNewNameNode(new RenameCoordinateCommand(dbService, true), true));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.getRoot().addChild(coordNode));
    }

    private ArgumentCommandNode<ServerCommandSource, String> getNameArgNode(final Command<ServerCommandSource> command,
                                                                            final boolean global,
                                                                            final boolean autocomplete) {
        final var comm = CommandManager.argument("name", string());
        if (autocomplete) {
            comm.suggests(new NameSuggestionProvider(dbService, global));
        }
        return comm.executes(command).build();
    }

    private ArgumentCommandNode<ServerCommandSource, String> getNewNameNode(final Command<ServerCommandSource> command,
                                                                            final boolean global) {
        final var nameComm = CommandManager.argument("name", string());
        nameComm.suggests(new NameSuggestionProvider(dbService, global));
        final var node = nameComm.build();
        final var comm = CommandManager.argument("newName", string());
        node.addChild(comm.executes(command).build());
        return node;
    }
}

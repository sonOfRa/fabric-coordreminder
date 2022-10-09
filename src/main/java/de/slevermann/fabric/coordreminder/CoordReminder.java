package de.slevermann.fabric.coordreminder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.slevermann.fabric.coordreminder.command.ClearCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.DeleteCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.GetCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.ListCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.NameSuggestionProvider;
import de.slevermann.fabric.coordreminder.command.SetCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.ShareCoordinateCommand;
import de.slevermann.fabric.coordreminder.command.TeleportCoordinateCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.mojang.brigadier.arguments.StringArgumentType.string;


public class CoordReminder implements DedicatedServerModInitializer {

    public static final UUID GLOBAL_COORDINATE_UUID = UUID.fromString("4c6b776e-1aa4-448b-a507-12c70461ff03");

    private static final Logger LOGGER = LoggerFactory.getLogger(CoordReminder.class);

    private static final Type TYPE = new TypeToken<ConcurrentHashMap<UUID, Map<String, Coordinate>>>() {
    }.getType();

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Coordinate.class, new CoordinateDeserializer())
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    private final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates = new ConcurrentHashMap<>();

    private boolean dataDirExists;

    @Override
    public void onInitializeServer() {
        LOGGER.info("Launching CoordReminder");
        final var coordNode = CommandManager.literal("coord").build();

        final var setNode = CommandManager.literal("set").build();
        coordNode.addChild(setNode);
        setNode.addChild(getNameArgNode(new SetCoordinateCommand(savedCoordinates, false), false));

        final var getNode = CommandManager.literal("get").build();
        coordNode.addChild(getNode);
        getNode.addChild(getNameArgNode(new GetCoordinateCommand(savedCoordinates, false), false, true));

        final var deleteNode = CommandManager.literal("delete").build();
        coordNode.addChild(deleteNode);
        deleteNode.addChild(getNameArgNode(new DeleteCoordinateCommand(savedCoordinates, false), false, true));

        final var teleportNode = CommandManager.literal("tp").build();
        coordNode.addChild(teleportNode);
        teleportNode.addChild(getNameArgNode(new TeleportCoordinateCommand(savedCoordinates, false), false, true));

        final var listNode = CommandManager.literal("list")
                .executes(new ListCoordinateCommand(savedCoordinates, false)).build();
        coordNode.addChild(listNode);

        final var shareNode = CommandManager.literal("share").build();
        coordNode.addChild(shareNode);
        shareNode.addChild(getNameArgNode(new ShareCoordinateCommand(savedCoordinates, false), false, true));

        final var clearNode = CommandManager.literal("clear")
                .executes(new ClearCoordinateCommand(savedCoordinates, false)).build();
        coordNode.addChild(clearNode);

        // Setup Global commands
        final var globalNode = CommandManager.literal("global").build();
        coordNode.addChild(globalNode);

        final var globalSetNode = CommandManager.literal("set").build();
        globalNode.addChild(globalSetNode);
        globalSetNode.addChild(getNameArgNode(new SetCoordinateCommand(savedCoordinates, true), true, false));

        final var globalGetNode = CommandManager.literal("get").build();
        globalNode.addChild(globalGetNode);
        globalGetNode.addChild(getNameArgNode(new GetCoordinateCommand(savedCoordinates, true), true, true));

        final var globalDeleteNode = CommandManager.literal("delete").build();
        globalNode.addChild(globalDeleteNode);
        globalDeleteNode.addChild(getNameArgNode(new DeleteCoordinateCommand(savedCoordinates, true), true, true));

        final var globalTeleportNode = CommandManager.literal("tp").build();
        globalNode.addChild(globalTeleportNode);
        globalTeleportNode.addChild(getNameArgNode(new TeleportCoordinateCommand(savedCoordinates, true), true, true));

        final var globalListNode = CommandManager.literal("list")
                .executes(new ListCoordinateCommand(savedCoordinates, true)).build();
        globalNode.addChild(globalListNode);

        final var globalShareNode = CommandManager.literal("share").build();
        globalNode.addChild(globalShareNode);
        globalShareNode.addChild(getNameArgNode(new ShareCoordinateCommand(savedCoordinates, true), true, true));

        final var globalClearNode = CommandManager.literal("clear")
                .executes(new ClearCoordinateCommand(savedCoordinates, true)).build();
        globalNode.addChild(globalClearNode);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.getRoot().addChild(coordNode));

        registerStartupEvent();

        registerShutdownEvent();
    }

    private ArgumentCommandNode<ServerCommandSource, String> getNameArgNode(Command<ServerCommandSource> command, boolean autocomplete) {
        return getNameArgNode(command, false, autocomplete);
    }

    private ArgumentCommandNode<ServerCommandSource, String> getNameArgNode(final Command<ServerCommandSource> command,
                                                                            final boolean global,
                                                                            final boolean autocomplete) {
        final var comm = CommandManager.argument("name", string());
        if (autocomplete) {
            comm.suggests(new NameSuggestionProvider(savedCoordinates, global));
        }
        return comm.executes(command).build();
    }

    private ArgumentCommandNode<ServerCommandSource, String> getNameArgNode(Command<ServerCommandSource> command) {
        return getNameArgNode(command, true);
    }

    private void registerShutdownEvent() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (dataDirExists) {
                final File coordinates = server.getFile("coordreminder/coordinates.json");
                try (FileWriter fw = new FileWriter(coordinates);
                     JsonWriter jw = new JsonWriter(fw)) {
                    GSON.toJson(this.savedCoordinates, TYPE, jw);
                } catch (IOException | JsonIOException e) {
                    LOGGER.error("Failed to write coordinate data", e);
                }
            } else {
                LOGGER.error("Mod directory does not exist, not saving data");
            }
        });
    }

    private void registerStartupEvent() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ConcurrentHashMap<UUID, Map<String, Coordinate>> coords = new ConcurrentHashMap<>();
            dataDirExists = createDataDir(server);
            if (dataDirExists) {
                final File coordinates = server.getFile("coordreminder/coordinates.json");
                if (!coordinates.exists()) {
                    LOGGER.info("Coordinate file does not exist yet, starting with an empty file");
                } else {
                    try (FileReader fr = new FileReader(coordinates);
                         JsonReader jr = new JsonReader(fr)) {
                        coords = GSON.fromJson(jr, TYPE);
                        final int playerCount = coords.size();
                        final int coordinateCount = coords.reduceValuesToInt(100, Map::size, 0, Integer::sum);
                        LOGGER.info("Successfully loaded {} coordinates for {} players", coordinateCount, playerCount);
                    } catch (IOException | JsonIOException e) {
                        LOGGER.error("Failed to read coordinate data", e);
                    }
                }
            }
            this.savedCoordinates.clear();
            this.savedCoordinates.putAll(coords);
        });
    }


    private boolean createDataDir(final MinecraftServer server) {
        final File directory = server.getFile("coordreminder");
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                LOGGER.error("Mod directory is not a directory, cannot save/load coordinates");
                return false;
            }
            return true;
        }
        final boolean dirs = directory.mkdirs();
        if (!dirs) {
            LOGGER.error("Failed to create mod directory, cannot save/load coordinates");
        }
        return dirs;
    }
}

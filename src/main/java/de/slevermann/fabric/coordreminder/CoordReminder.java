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

        final GetCoordinateCommand getCommand = new GetCoordinateCommand(savedCoordinates);
        final SetCoordinateCommand setCommand = new SetCoordinateCommand(savedCoordinates);
        final DeleteCoordinateCommand deleteCommand = new DeleteCoordinateCommand(savedCoordinates);
        final TeleportCoordinateCommand teleportCommand = new TeleportCoordinateCommand(savedCoordinates);
        final ListCoordinateCommand listCommand = new ListCoordinateCommand(savedCoordinates);
        final ShareCoordinateCommand shareCommand = new ShareCoordinateCommand(savedCoordinates);
        final ClearCoordinateCommand clearCommand = new ClearCoordinateCommand(savedCoordinates);

        final LiteralCommandNode<ServerCommandSource> coordNode = CommandManager
                .literal("coord").build();

        final LiteralCommandNode<ServerCommandSource> getNode = CommandManager
                .literal("get").build();
        coordNode.addChild(getNode);
        getNode.addChild(getNameArgNode(getCommand));

        final LiteralCommandNode<ServerCommandSource> setNode = CommandManager
                .literal("set").build();
        coordNode.addChild(setNode);
        setNode.addChild(getNameArgNode(setCommand, false));

        final LiteralCommandNode<ServerCommandSource> deleteNode = CommandManager
                .literal("delete").build();
        coordNode.addChild(deleteNode);
        deleteNode.addChild(getNameArgNode(deleteCommand));

        final LiteralCommandNode<ServerCommandSource> teleportNode = CommandManager
                .literal("tp").build();
        coordNode.addChild(teleportNode);
        teleportNode.addChild(getNameArgNode(teleportCommand));

        final LiteralCommandNode<ServerCommandSource> listNode = CommandManager
                .literal("list")
                .executes(listCommand).build();
        coordNode.addChild(listNode);

        final LiteralCommandNode<ServerCommandSource> shareNode = CommandManager
                .literal("share").build();
        coordNode.addChild(shareNode);
        shareNode.addChild(getNameArgNode(shareCommand));

        final LiteralCommandNode<ServerCommandSource> clearNode = CommandManager
                .literal("clear")
                .executes(clearCommand).build();
        coordNode.addChild(clearNode);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.getRoot().addChild(coordNode));

        registerStartupEvent();

        registerShutdownEvent();
    }

    private ArgumentCommandNode<ServerCommandSource, String> getNameArgNode(Command<ServerCommandSource> command, boolean autocomplete) {
        final var comm = CommandManager.argument("name", string());
        if (autocomplete) {
            comm.suggests(new NameSuggestionProvider(savedCoordinates));
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

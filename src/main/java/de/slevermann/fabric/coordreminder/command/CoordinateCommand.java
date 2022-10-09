package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static de.slevermann.fabric.coordreminder.CoordReminder.GLOBAL_COORDINATE_UUID;
import static net.minecraft.text.Text.literal;

public abstract class CoordinateCommand implements Command<ServerCommandSource> {

    protected final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates;

    protected final boolean global;

    public CoordinateCommand(ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        this(savedCoordinates, false);
    }

    public CoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                             final boolean global) {
        this.savedCoordinates = savedCoordinates;
        this.global = global;
    }

    public final UUID getUuid(final CommandContext<ServerCommandSource> context) {
        return global ? GLOBAL_COORDINATE_UUID : getPlayer(context).getUuid();
    }

    @Override
    public final int run(CommandContext<ServerCommandSource> context) {
        // Ensure the player has a map in memory
        savedCoordinates.putIfAbsent(getUuid(context), new HashMap<>());
        return runCommand(context);
    }

    public abstract int runCommand(CommandContext<ServerCommandSource> context);

    public final ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context) {
        return context.getSource().getPlayer();
    }

    public final MutableText formatCoordinateforChat(Coordinate coord) {
        return literal("")
                .append(literal(String.format("World: %s", coord.getDimension())).formatted(Formatting.AQUA))
                .append(literal("; "))
                .append(literal(String.format("X: %d", coord.getX())).formatted(Formatting.RED))
                .append(literal("; "))
                .append(literal(String.format("Y: %d", coord.getY())).formatted(Formatting.GREEN))
                .append(literal("; "))
                .append(literal(String.format("Z: %d", coord.getZ())).formatted(Formatting.BLUE));
    }

    public final Map<String, Coordinate> getCoordinateMap(CommandContext<ServerCommandSource> context) {
        return savedCoordinates.get(getUuid(context));
    }
}

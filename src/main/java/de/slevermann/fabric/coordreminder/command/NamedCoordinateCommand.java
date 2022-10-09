package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static net.minecraft.text.Text.of;

public abstract class NamedCoordinateCommand extends CoordinateCommand {

    public NamedCoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        this(savedCoordinates, false);
    }

    public NamedCoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                                  final boolean global) {
        super(savedCoordinates, global);
    }

    public final String getCoordinateName(final CommandContext<ServerCommandSource> context) {
        return context.getArgument("name", String.class);
    }

    public final Coordinate getCoordinate(final CommandContext<ServerCommandSource> context) {
        return savedCoordinates.get(getUuid(context)).get(getCoordinateName(context));
    }

    public final void missingCoordinate(final CommandContext<ServerCommandSource> context) {
        getPlayer(context).sendMessage(of(format("%s coordinate %s does not exist", global ? "Global" : "Personal",
                getCoordinateName(context))), false);
    }
}

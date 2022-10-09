package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static net.minecraft.text.Text.of;

public abstract class MoveCommand extends NamedCoordinateCommand {

    public MoveCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                       final boolean global) {
        super(savedCoordinates, global);
    }

    public final String getNewCoordinateName(final CommandContext<ServerCommandSource> context) {
        return context.getArgument("newName", String.class);
    }

}

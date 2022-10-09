package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GetCoordinateCommand extends NamedCoordinateCommand {

    public GetCoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                                final boolean global) {
        super(savedCoordinates, global);
    }

    @Override
    public int runCommand(final CommandContext<ServerCommandSource> context) {
        final Coordinate coord = getCoordinate(context);
        if (coord != null) {
            getPlayer(context).sendMessage(formatCoordinateforChat(coord), false);
            return 1;
        }
        missingCoordinate(context);
        return -1;
    }

}

package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.text.Text.literal;

public class ShareCoordinateCommand extends NamedCoordinateCommand {

    public ShareCoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        super(savedCoordinates);
    }

    public ShareCoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                                  final boolean global) {
        super(savedCoordinates, global);
    }

    @Override
    public int runCommand(final CommandContext<ServerCommandSource> context) {
        final Coordinate coord = getCoordinate(context);
        if (coord != null) {
            final MutableText text = literal(String.format("%s shared %s coordinate %s: ",
                    getPlayer(context).getName().getString(), global ? "global" : "personal",
                    getCoordinateName(context))).append(formatCoordinateforChat(coord));
            context.getSource().getServer().getPlayerManager().broadcast(text, false);
            return 1;
        }
        missingCoordinate(context);
        return -1;
    }
}

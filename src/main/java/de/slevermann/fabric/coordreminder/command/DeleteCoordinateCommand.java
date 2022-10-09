package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static net.minecraft.text.Text.of;

public class DeleteCoordinateCommand extends NamedCoordinateCommand {

    public DeleteCoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                                   final boolean global) {
        super(savedCoordinates, global);
    }

    @Override
    public int runCommand(CommandContext<ServerCommandSource> context) {
        final Coordinate coord = getCoordinate(context);

        if (coord != null) {
            getCoordinateMap(context).remove(getCoordinateName(context));
            getPlayer(context).sendMessage(of(format("Deleted %s coordinate %s", global ? "global" : "personal",
                    getCoordinateName(context))), false);
            return 1;
        }
        missingCoordinate(context);
        return -1;
    }
}

package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static net.minecraft.text.Text.of;

public class RenameCommand extends MoveCommand {

    public RenameCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                         final boolean global) {
        super(savedCoordinates, global);
    }

    @Override
    public int runCommand(CommandContext<ServerCommandSource> context) {
        final var oldName = getCoordinateName(context);
        final var newName = getNewCoordinateName(context);
        final var map = getCoordinateMap(context);

        if (map.containsKey(newName)) {
            getPlayer(context).sendMessage(of(format("%s coordinate %s already exists", global ? "Global" : "Personal",
                    newName)));
            return -1;
        }

        final var coordinate = map.remove(oldName);
        if (coordinate != null) {
            map.put(newName, coordinate);
            getPlayer(context).sendMessage(of(format("Renamed %s coordinate from %s to %s",
                    global ? "global" : "personal", oldName, newName)));
            return 1;
        }
        missingCoordinate(context);
        return -2;
    }
}

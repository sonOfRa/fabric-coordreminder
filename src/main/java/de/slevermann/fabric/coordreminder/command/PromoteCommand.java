package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import de.slevermann.fabric.coordreminder.CoordReminder;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static de.slevermann.fabric.coordreminder.CoordReminder.GLOBAL_COORDINATE_UUID;
import static java.lang.String.format;
import static net.minecraft.text.Text.of;

public class PromoteCommand extends MoveCommand {

    public PromoteCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        super(savedCoordinates, false);
    }

    @Override
    public int runCommand(CommandContext<ServerCommandSource> context) {
        final var personalName = getCoordinateName(context);
        final var globalName = getNewCoordinateName(context);
        final var map = getCoordinateMap(context);
        final var globalMap = savedCoordinates.get(GLOBAL_COORDINATE_UUID);

        if (globalMap.containsKey(globalName)) {
            getPlayer(context).sendMessage(of(format("Global coordinate %s already exists", globalName)));
            return -1;
        }

        final var coordinate = map.remove(personalName);
        if (coordinate != null) {
            globalMap.put(globalName, coordinate);
            getPlayer(context).sendMessage(of(format("Moved personal coordinate %s to global coordinate %s",
                    personalName, globalName)));
            return 1;
        }

        missingCoordinate(context);
        return -2;
    }

}

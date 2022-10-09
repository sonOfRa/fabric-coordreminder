package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static net.minecraft.text.Text.of;

public class ClearCoordinateCommand extends CoordinateCommand {

    public ClearCoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                                  final boolean global) {
        super(savedCoordinates, global);
    }

    @Override
    public int runCommand(CommandContext<ServerCommandSource> context) {
        final var player = getPlayer(context);
        if (global && !player.hasPermissionLevel(4)) {
            player.sendMessage(of("Only admins can clear global coordinates"));
            return -1;
        }
        getCoordinateMap(context).clear();
        getPlayer(context).sendMessage(of(format("All %s coordinates deleted!", global ? "global" : "personal")));
        return 1;
    }
}

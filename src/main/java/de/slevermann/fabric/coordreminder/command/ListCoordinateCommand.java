package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static net.minecraft.text.Text.literal;
import static net.minecraft.text.Text.of;

public class ListCoordinateCommand extends CoordinateCommand {

    public ListCoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                                 final boolean global) {
        super(savedCoordinates, global);
    }

    @Override
    public int runCommand(final CommandContext<ServerCommandSource> context) {
        final ServerPlayerEntity player = getPlayer(context);
        player.sendMessage(of(format("List of %s coordinates:", global ? "global" : "personal")), false);
        for (Map.Entry<String, Coordinate> coordinate : getCoordinateMap(context).entrySet()) {
            final MutableText text = literal(coordinate.getKey() + ": ")
                    .append(formatCoordinateforChat(coordinate.getValue()));
            player.sendMessage(text, false);
        }
        return 1;
    }
}

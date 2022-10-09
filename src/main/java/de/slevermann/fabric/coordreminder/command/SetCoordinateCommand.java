package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static net.minecraft.text.Text.of;

public class SetCoordinateCommand extends NamedCoordinateCommand {

    public SetCoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                                boolean global) {
        super(savedCoordinates, global);
    }

    @Override
    public int runCommand(final CommandContext<ServerCommandSource> context) {
        final Coordinate coord = getCoordinate(context);
        final var coordName = getCoordinateName(context);
        if (coord == null) {
            final PlayerEntity player = getPlayer(context);
            final String dimension = player.getWorld().getRegistryKey().getValue().toString();
            final Coordinate newCoordinate = new Coordinate(dimension, player.getBlockX(), player.getBlockY(),
                    player.getBlockZ());
            getCoordinateMap(context).put(coordName, newCoordinate);
            player.sendMessage(of(format("Saved %s coordinate %s at the current location",
                    global ? "global" : "personal", coordName)), false);
            return 1;
        }
        getPlayer(context)
                .sendMessage(of(format("%s coordinate %s already exists. Must be deleted before it can be reused",
                        global ? "Global" : "Personal", coordName)), false);
        return -1;
    }
}

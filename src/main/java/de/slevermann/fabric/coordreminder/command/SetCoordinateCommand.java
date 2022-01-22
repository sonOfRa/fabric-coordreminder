package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SetCoordinateCommand extends NamedCoordinateCommand {

    public SetCoordinateCommand(ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        super(savedCoordinates);
    }

    @Override
    public int runCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final Coordinate coord = getCoordinate(context);
        if (coord == null) {
            final PlayerEntity player = getPlayer(context);
            final String dimension = player.getWorld().getRegistryKey().getValue().toString();
            final Coordinate newCoordinate = new Coordinate(dimension, player.getBlockX(),
                    player.getBlockY(), player.getBlockZ());
            getPlayerMap(context).put(getName(context), newCoordinate);
            player.sendMessage(Text.of(String.format("Saved coordinate %s at the current location", getName(context))), false);
            return 1;
        }
        getPlayer(context).sendMessage(Text.of(String.format("Coordinate %s already exists. Must be deleted before it can be reused",
                getName(context))), false);
        return -1;
    }
}

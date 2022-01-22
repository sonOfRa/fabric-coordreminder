package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ListCoordinateCommand extends CoordinateCommand {

    public ListCoordinateCommand(ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        super(savedCoordinates);
    }

    @Override
    public int runCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = getPlayer(context);
        player.sendMessage(Text.of("List of saved coordinates:"), false);
        for (Map.Entry<String, Coordinate> coordinate : getPlayerMap(context).entrySet()) {
            final MutableText text = new LiteralText(coordinate.getKey() + ": ")
                    .append(formatCoordinateforChat(coordinate.getValue()));
            player.sendMessage(text, false);
        }
        return 1;
    }
}

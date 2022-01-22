package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShareCoordinateCommand extends NamedCoordinateCommand {

    public ShareCoordinateCommand(ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        super(savedCoordinates);
    }

    @Override
    public int runCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final Coordinate coord = getCoordinate(context);
        if (coord != null) {
            final MutableText text = new LiteralText(String.format("%s shared coordinate %s: ",
                    getPlayer(context).getName().asString(), getName(context)))
                    .append(formatCoordinateforChat(coord));
            context.getSource().getServer().getPlayerManager().broadcast(text, MessageType.CHAT,
                    getPlayer(context).getUuid());
            return 1;
        }
        missingCoordinate(context);
        return -1;
    }
}

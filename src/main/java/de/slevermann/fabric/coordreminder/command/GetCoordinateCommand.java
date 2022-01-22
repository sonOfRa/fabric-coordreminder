package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GetCoordinateCommand extends NamedCoordinateCommand {

    public GetCoordinateCommand(ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        super(savedCoordinates);
    }

    @Override
    public int runCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final Coordinate coord = getCoordinate(context);
        if (coord != null) {
            getPlayer(context).sendMessage(formatCoordinateforChat(coord), false);
            return 1;
        }
        missingCoordinate(context);
        return -1;
    }

}

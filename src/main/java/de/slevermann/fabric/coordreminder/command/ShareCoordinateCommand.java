package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import net.minecraft.server.command.ServerCommandSource;

import static java.lang.String.format;
import static net.minecraft.text.Text.literal;

public class ShareCoordinateCommand extends NamedCoordinateCommand {

    public ShareCoordinateCommand(final CoordinateService coordinateService, final boolean global) {
        super(coordinateService, global);
    }

    @Override
    public int run(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var coordinate = getCoordinate(context);
        if (coordinate == null) {
            throw COORDINATE_NOT_FOUND.create(global, getCoordinateName(context));
        }
        final var text = literal(format("%s shared %s coordinate %s: ",
                getPlayer(context).getName().getString(), global ? "global" : "personal", coordinate.name()))
                .append(formatCoordinateforChat(coordinate));
        context.getSource().getServer().getPlayerManager().broadcast(text, false);
        return SINGLE_SUCCESS;
    }
}

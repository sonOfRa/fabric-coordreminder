package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import net.minecraft.server.command.ServerCommandSource;

public class GetCoordinateCommand extends NamedCoordinateCommand {

    public GetCoordinateCommand(final CoordinateService coordinateService, final boolean global) {
        super(coordinateService, global);
    }

    @Override
    public int run(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var coordinate = getCoordinate(context);
        if (coordinate == null) {
            throw COORDINATE_NOT_FOUND.create(global, getCoordinateName(context));
        }
        context.getSource().sendFeedback(() -> formatCoordinateforChat(coordinate), false);
        return SINGLE_SUCCESS;
    }
}

package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static java.lang.String.format;
import static net.minecraft.text.Text.of;

public class DeleteCoordinateCommand extends NamedCoordinateCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCoordinateCommand.class);

    public DeleteCoordinateCommand(final CoordinateService coordinateService, final boolean global) {
        super(coordinateService, global);
    }

    @Override
    public int run(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var coordinateName = getCoordinateName(context);
        try {
            if (!coordinateService.deleteCoordinate(coordinateName, getUuid(context))) {
                throw COORDINATE_NOT_FOUND.create(global, coordinateName);
            }
        } catch (final SQLException e) {
            throw DB_ERROR.create(LOGGER, e);
        }
        context.getSource().sendFeedback(() -> of(format("%s coordinate %s deleted!",
                global ? "Global" : "Personal", coordinateName)), false);
        return SINGLE_SUCCESS;
    }
}

package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static de.slevermann.fabric.coordreminder.CoordReminder.GLOBAL_COORDINATE_UUID;
import static java.lang.String.format;
import static net.minecraft.text.Text.of;

public class PromoteCoordinateCommand extends MoveCoordinateCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromoteCoordinateCommand.class);

    public PromoteCoordinateCommand(final CoordinateService coordinateService) {
        super(coordinateService, false);
    }

    @Override
    public int run(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var name = getCoordinateName(context);
        final var globalName = getNewCoordinateName(context);
        final var owner = getUuid(context);
        try {
            int result = coordinateService.moveCoordinate(name, owner, globalName, GLOBAL_COORDINATE_UUID);
            if (result == -1) {
                throw COORDINATE_EXISTS.create(true, name);
            }
            if (result == 0) {
                throw COORDINATE_NOT_FOUND.create(false, globalName);
            }
        } catch (SQLException e) {
            throw DB_ERROR.create(LOGGER, e);
        }
        context.getSource().sendFeedback(of(format("Personal coordinate %s is now global coordinate %s",
                name, globalName)), false);
        return SINGLE_SUCCESS;
    }
}

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

public class RenameCoordinateCommand extends MoveCoordinateCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenameCoordinateCommand.class);

    public RenameCoordinateCommand(final CoordinateService coordinateService, final boolean global) {
        super(coordinateService, global);
    }

    @Override
    public int run(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var owner = getUuid(context);
        final var oldName = getCoordinateName(context);
        final var newName = getNewCoordinateName(context);
        try {
            int result = coordinateService.moveCoordinate(oldName, owner, newName, owner);
            if (result == -1) {
                throw COORDINATE_EXISTS.create(global, newName);
            }
            if (result == 0) {
                throw COORDINATE_NOT_FOUND.create(global, oldName);
            }
        } catch (SQLException e) {
            throw DB_ERROR.create(LOGGER, e);
        }
        context.getSource().sendFeedback(() -> of(format("%s coordinate %s is now named %s",
                global ? "Global" : "Personal", oldName, newName)), false);
        return SINGLE_SUCCESS;
    }
}

package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import de.slevermann.fabric.coordreminder.db.DbCoordinate;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public abstract class NamedCoordinateCommand extends CoordinateCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(NamedCoordinateCommand.class);

    public NamedCoordinateCommand(final CoordinateService coordinateService, final boolean global) {
        super(coordinateService, global);
    }

    public final String getCoordinateName(final CommandContext<ServerCommandSource> context) {
        return context.getArgument("name", String.class);
    }

    public final DbCoordinate getCoordinate(final CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        try {
            return coordinateService.getCoordinate(getCoordinateName(context), getUuid(context));
        } catch (final SQLException e) {
            throw DB_ERROR.create(LOGGER, e);
        }
    }
}

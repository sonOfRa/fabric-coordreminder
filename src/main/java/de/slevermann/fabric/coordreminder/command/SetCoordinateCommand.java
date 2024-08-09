package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import de.slevermann.fabric.coordreminder.db.DbCoordinate;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static java.lang.String.format;
import static net.minecraft.text.Text.of;

public class SetCoordinateCommand extends NamedCoordinateCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetCoordinateCommand.class);

    public SetCoordinateCommand(final CoordinateService coordinateService, final boolean global) {
        super(coordinateService, global);
    }

    @Override
    public int run(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var coordinateName = getCoordinateName(context);
        final var player = getPlayer(context);
        final var registry = player.getWorld().getRegistryKey().getRegistry().toString();
        final var registryValue = player.getWorld().getRegistryKey().getValue().toString();
        final var coordinate = new DbCoordinate(coordinateName, registry, registryValue, getUuid(context),
                player.getX(), player.getY(), player.getZ());
        try {
            if (!coordinateService.createCoordinate(coordinate)) {
                throw COORDINATE_EXISTS.create(global, coordinateName);
            }
        } catch (final SQLException e) {
            throw DB_ERROR.create(LOGGER, e);
        }
        context.getSource().sendFeedback(() -> of(format("Coordinate %s created", coordinateName)), false);
        return SINGLE_SUCCESS;
    }
}

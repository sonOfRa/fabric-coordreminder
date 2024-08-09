package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import de.slevermann.fabric.coordreminder.db.DbCoordinate;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

import static java.lang.String.format;
import static net.minecraft.text.Text.literal;

public class ListCoordinateCommand extends CoordinateCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCoordinateCommand.class);

    public ListCoordinateCommand(final CoordinateService coordinateService, final boolean global) {
        super(coordinateService, global);
    }

    @Override
    public int run(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final List<DbCoordinate> coordinates;
        try {
            coordinates = coordinateService.listCoordinates(getUuid(context));
        } catch (SQLException e) {
            throw DB_ERROR.create(LOGGER, e);
        }

        final var text = literal(format("List of %s coordinates:%n", global ? "global" : "personal"));
        for (int i = 0; i < coordinates.size(); i++) {
            final var coord = coordinates.get(i);
            text.append(format("%s: ", coord.name())).append(formatCoordinateforChat(coord));
            if (i < coordinates.size() - 1) {
                text.append(System.lineSeparator());
            }
        }
        context.getSource().sendFeedback(() -> text, false);
        return SINGLE_SUCCESS;
    }
}

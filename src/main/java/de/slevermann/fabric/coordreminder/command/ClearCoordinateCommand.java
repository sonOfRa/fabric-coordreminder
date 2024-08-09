package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class ClearCoordinateCommand extends CoordinateCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClearCoordinateCommand.class);

    public ClearCoordinateCommand(final CoordinateService coordinateService, final boolean global) {
        super(coordinateService, global);
    }

    @Override
    public int run(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            int deletedCount = coordinateService.clearCoordinates(getUuid(context));
            context.getSource().sendFeedback(() -> Text.literal(String.format("Successfully deleted %d %s coordinates",
                    deletedCount, global ? "global" : "personal")), false);
        } catch (SQLException e) {
            throw DB_ERROR.create(LOGGER, e);
        }
        return SINGLE_SUCCESS;
    }
}

package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import de.slevermann.fabric.coordreminder.db.DbCoordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static de.slevermann.fabric.coordreminder.CoordReminder.GLOBAL_COORDINATE_UUID;
import static java.lang.String.format;
import static net.minecraft.text.Text.literal;
import static net.minecraft.text.Text.of;

public abstract class CoordinateCommand implements Command<ServerCommandSource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateCommand.class);

    protected final CoordinateService coordinateService;

    protected final boolean global;

    protected static final Dynamic2CommandExceptionType DB_ERROR = new Dynamic2CommandExceptionType((logger, exception) -> {
        ((Logger) logger).error("Database error", (Throwable) exception);
        return of("Failed to communicated with coordinate database. Check the server log for errors");
    });

    protected static final Dynamic2CommandExceptionType COORDINATE_EXISTS = new Dynamic2CommandExceptionType((a, b) ->
            of(format("%s coordinate with name %s already exists!", ((boolean) a) ? "Global" : "Personal", b)));

    protected static final Dynamic2CommandExceptionType COORDINATE_NOT_FOUND = new Dynamic2CommandExceptionType((a, b) ->
            of(format("%s coordinate with name %s not found!", ((boolean) a) ? "Global" : "Personal", b)));

    public CoordinateCommand(final CoordinateService coordinateService, final boolean global) {
        this.coordinateService = coordinateService;
        this.global = global;
    }

    public final ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context) {
        return context.getSource().getPlayer();
    }

    public final UUID getUuid(final CommandContext<ServerCommandSource> context) {
        return global ? GLOBAL_COORDINATE_UUID : getPlayer(context).getUuid();
    }

    public final MutableText formatCoordinateforChat(DbCoordinate coord) {
        return literal("")
                .append(literal(format("World: %s", coord.registryValue())).formatted(Formatting.AQUA))
                .append(literal("; "))
                .append(literal(format("X: %d", ((int) coord.x()))).formatted(Formatting.RED))
                .append(literal("; "))
                .append(literal(format("Y: %d", ((int) coord.y()))).formatted(Formatting.GREEN))
                .append(literal("; "))
                .append(literal(format("Z: %d", ((int) coord.z()))).formatted(Formatting.BLUE));
    }
}

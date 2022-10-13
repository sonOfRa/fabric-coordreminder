package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import net.minecraft.server.command.ServerCommandSource;

public abstract class MoveCoordinateCommand extends NamedCoordinateCommand {

    public MoveCoordinateCommand(final CoordinateService coordinateService, final boolean global) {
        super(coordinateService, global);
    }

    public final String getNewCoordinateName(final CommandContext<ServerCommandSource> context) {
        return context.getArgument("newName", String.class);
    }

}

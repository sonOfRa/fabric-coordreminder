package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NamedCoordinateCommand extends CoordinateCommand {

    public NamedCoordinateCommand(ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        super(savedCoordinates);
    }

    public final String getName(CommandContext<ServerCommandSource> context) {
        return context.getArgument("name", String.class);
    }

    public final Coordinate getCoordinate(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return savedCoordinates.get(getPlayer(context).getUuid()).get(getName(context));
    }

    public final void missingCoordinate(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        getPlayer(context).sendMessage(Text.of(String.format("Coordinate %s does not exist", getName(context))), false);
    }
}

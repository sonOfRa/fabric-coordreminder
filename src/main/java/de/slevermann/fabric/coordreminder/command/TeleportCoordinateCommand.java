package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import de.slevermann.fabric.coordreminder.db.CoordinateService;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;

import static java.lang.String.format;
import static net.minecraft.text.Text.literal;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Identifier.tryParse;
import static net.minecraft.registry.RegistryKey.ofRegistry;

public class TeleportCoordinateCommand extends NamedCoordinateCommand {

    protected static final Dynamic4CommandExceptionType WORLD_NOT_FOUND = new Dynamic4CommandExceptionType((a, b, c, d) ->
            of(format("World (%s[%s]) for %s coordinate with name %s not found!",
                    a, b, ((boolean) c) ? "global" : "personal", d)));

    public TeleportCoordinateCommand(final CoordinateService coordinateService, final boolean global) {
        super(coordinateService, global);
    }

    @Override
    public int run(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var coordinateName = getCoordinateName(context);
        final var coordinate = getCoordinate(context);

        if (coordinate == null) {
            throw COORDINATE_NOT_FOUND.create(global, coordinateName);
        }
        final RegistryKey<Registry<World>> registry = ofRegistry(tryParse(coordinate.registry()));
        final ServerWorld world;
        if (registry == null) {
            world = null;
        } else {
            final var key = RegistryKey.of(registry, tryParse(coordinate.registryValue()));
            if (key == null) {
                world = null;
            } else {
                world = context.getSource().getServer().getWorld(key);
            }
        }

        if (world == null) {
            throw WORLD_NOT_FOUND.create(coordinate.registry(), coordinate.registryValue(), global, coordinateName);
        }
        final var player = getPlayer(context);
        player.teleport(world, coordinate.x(), coordinate.y(), coordinate.z(), player.getYaw(), player.getPitch());
        context.getSource().sendFeedback(() -> literal("Teleported to: ").append(formatCoordinateforChat(coordinate)),
                false);
        return SINGLE_SUCCESS;
    }
}

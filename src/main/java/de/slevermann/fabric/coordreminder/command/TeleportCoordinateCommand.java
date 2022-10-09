package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Identifier.tryParse;
import static net.minecraft.util.registry.RegistryKey.ofRegistry;

public class TeleportCoordinateCommand extends NamedCoordinateCommand {

    private static final RegistryKey<Registry<World>> REGISTRY = ofRegistry(tryParse("minecraft:dimension"));

    public TeleportCoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        this(savedCoordinates, false);
    }

    public TeleportCoordinateCommand(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                                     final boolean global) {
        super(savedCoordinates, global);
    }

    @Override
    public int runCommand(final CommandContext<ServerCommandSource> context) {
        final Coordinate coord = getCoordinate(context);

        if (coord != null) {
            final ServerPlayerEntity player = getPlayer(context);
            final RegistryKey<World> key = RegistryKey.of(REGISTRY, tryParse(coord.getDimension()));
            if (key == null) {
                player.sendMessage(of(format("World %s for %s coordinate %s does not exist", coord.getDimension(),
                        global ? "global" : "personal", getCoordinateName(context))), false);
                return -1;
            }
            final ServerWorld world = context.getSource().getServer().getWorld(key);
            if (world == null) {
                player.sendMessage(of(format("World %s for %s coordinate %s does not exist", coord.getDimension(),
                                global ? "global" : "personal", getCoordinateName(context))),
                        false);
                return -1;
            }
            player.teleport(world, coord.getX(), coord.getY(), coord.getZ(), player.getYaw(), player.getPitch());
            return 1;
        }
        missingCoordinate(context);
        return -2;
    }
}

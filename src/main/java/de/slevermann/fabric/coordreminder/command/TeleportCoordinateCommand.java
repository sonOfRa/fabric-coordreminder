package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportCoordinateCommand extends NamedCoordinateCommand {

    private static final RegistryKey<Registry<World>> REGISTRY = RegistryKey.ofRegistry(Identifier.tryParse("minecraft:dimension"));

    public TeleportCoordinateCommand(ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        super(savedCoordinates);
    }

    @Override
    public int runCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final Coordinate coord = getCoordinate(context);

        if (coord != null) {
            final ServerPlayerEntity player = getPlayer(context);
            final RegistryKey<World> key = RegistryKey.of(REGISTRY, Identifier.tryParse(coord.getDimension()));
            if (key == null) {
                player.sendMessage(Text.of(String.format("World %s for coordinate %s does not exist",
                        coord.getDimension(), getName(context))), false);
                return -1;
            }
            final ServerWorld world = context.getSource().getServer().getWorld(key);
            if (world == null) {
                player.sendMessage(Text.of(String.format("World %s for coordinate %s does not exist",
                        coord.getDimension(), getName(context))), false);
                return -1;
            }
            player.teleport(world, coord.getX(), coord.getY(), coord.getZ(), player.getYaw(), player.getPitch());
            return 1;
        }
        missingCoordinate(context);
        return -2;
    }
}

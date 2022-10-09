package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.slevermann.fabric.coordreminder.Coordinate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static de.slevermann.fabric.coordreminder.CoordReminder.GLOBAL_COORDINATE_UUID;

public class NameSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    private final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates;

    private final boolean global;

    public NameSuggestionProvider(final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates,
                                  final boolean global) {
        this.savedCoordinates = savedCoordinates;
        this.global = global;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<ServerCommandSource> context,
                                                         final SuggestionsBuilder builder) {
        final var uuid = global ? GLOBAL_COORDINATE_UUID : getPlayer(context).getUuid();
        savedCoordinates.putIfAbsent(uuid, new HashMap<>());
        final Map<String, Coordinate> coordinates = savedCoordinates.get(uuid);
        for (String name : coordinates.keySet()) {
            if (name.startsWith(builder.getRemaining())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }

    private ServerPlayerEntity getPlayer(final CommandContext<ServerCommandSource> context) {
        return context.getSource().getPlayer();
    }

}

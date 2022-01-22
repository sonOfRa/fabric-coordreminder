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

public class NameSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    protected final ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates;

    public NameSuggestionProvider(ConcurrentHashMap<UUID, Map<String, Coordinate>> savedCoordinates) {
        this.savedCoordinates = savedCoordinates;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        savedCoordinates.putIfAbsent(getPlayer(context).getUuid(), new HashMap<>());
        final Map<String, Coordinate> playerCoordinates = savedCoordinates.get(getPlayer(context).getUuid());
        for (String name : playerCoordinates.keySet()) {
            if (name.startsWith(builder.getRemaining())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }

    private ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return context.getSource().getPlayer();
    }

}

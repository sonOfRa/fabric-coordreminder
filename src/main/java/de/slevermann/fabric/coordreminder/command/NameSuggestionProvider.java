package de.slevermann.fabric.coordreminder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.slevermann.fabric.coordreminder.db.CoordinateService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

import static de.slevermann.fabric.coordreminder.CoordReminder.GLOBAL_COORDINATE_UUID;

public class NameSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NameSuggestionProvider.class);

    private final CoordinateService coordinateService;

    private final boolean global;

    public NameSuggestionProvider(final CoordinateService coordinateService, final boolean global) {
        this.coordinateService = coordinateService;
        this.global = global;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<ServerCommandSource> context,
                                                         final SuggestionsBuilder builder) {
        final var uuid = global ? GLOBAL_COORDINATE_UUID : getPlayer(context).getUuid();
        try {
            final var names = coordinateService.findCoordinateNames(builder.getRemaining(), uuid);
            for (final var name : names) {
                builder.suggest(name);
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to fetch coordinates for autocompletion", e);
        }
        return builder.buildFuture();
    }

    private ServerPlayerEntity getPlayer(final CommandContext<ServerCommandSource> context) {
        return context.getSource().getPlayer();
    }

}

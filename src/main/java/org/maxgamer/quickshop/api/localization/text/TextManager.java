package org.maxgamer.quickshop.api.localization.text;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface TextManager {
    /**
     * Gets specific locale status
     *
     * @param locale The locale
     * @param regex  The regexes
     * @return The locale enabled status
     */
    boolean localeEnabled(@NotNull String locale, @NotNull List<String> regex);


    /**
     * Getting the translation with path with default locale
     *
     * @param path THe path
     * @param args The arguments
     * @return The text object
     */
    @NotNull
    Text of(@NotNull String path, String... args);

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The sender
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @NotNull
    Text of(@Nullable CommandSender sender, @NotNull String path, String... args);

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The player unique id
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @NotNull
    Text of(@Nullable UUID sender, @NotNull String path, String... args);

    /**
     * Getting the translation with path with default locale (if available)
     *
     * @param path The path
     * @param args The arguments
     * @return The text object
     */
    @NotNull
    TextList ofList(@NotNull String path, String... args);

    /**
     * Getting the translation with path  with player's locale (if available)
     *
     * @param sender The player unique id
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @NotNull
    TextList ofList(@Nullable UUID sender, @NotNull String path, String... args);

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The player
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @NotNull
    TextList ofList(@Nullable CommandSender sender, @NotNull String path, String... args);
}

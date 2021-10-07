package org.maxgamer.quickshop.api.localization.text.postprocessor;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PostProcessor {
    /**
     * Process the string
     *
     * @param text   Original string
     * @param sender The command sender
     * @param args   The arguments
     * @return The string that processed
     */
    @NotNull
    String process(@NotNull String text, @Nullable CommandSender sender, String... args);
}

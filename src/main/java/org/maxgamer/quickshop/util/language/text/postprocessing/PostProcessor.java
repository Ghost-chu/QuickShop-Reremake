package org.maxgamer.quickshop.util.language.text.postprocessing;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PostProcessor {
    @NotNull
    String process(@NotNull String text, @Nullable CommandSender sender, String... args);
}

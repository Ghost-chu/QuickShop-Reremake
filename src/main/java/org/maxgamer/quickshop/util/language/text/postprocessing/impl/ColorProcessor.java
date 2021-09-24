package org.maxgamer.quickshop.util.language.text.postprocessing.impl;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.language.text.postprocessing.PostProcessor;

public class ColorProcessor implements PostProcessor {
    @Override
    public @NotNull String process(@NotNull String text, @Nullable CommandSender sender, String... args) {
        return Util.parseColours(text);
    }
}

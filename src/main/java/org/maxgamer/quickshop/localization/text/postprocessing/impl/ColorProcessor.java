package org.maxgamer.quickshop.localization.text.postprocessing.impl;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.localization.text.postprocessor.PostProcessor;
import org.maxgamer.quickshop.util.Util;

public class ColorProcessor implements PostProcessor {
    @Override
    public @NotNull String process(@NotNull String text, @Nullable CommandSender sender, String... args) {
        return Util.parseColours(text);
    }

}

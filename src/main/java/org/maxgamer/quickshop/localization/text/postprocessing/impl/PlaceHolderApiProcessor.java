package org.maxgamer.quickshop.localization.text.postprocessing.impl;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.localization.text.postprocessor.PostProcessor;

public class PlaceHolderApiProcessor implements PostProcessor {
    @Override
    public @NotNull String process(@NotNull String text, @Nullable CommandSender sender, String... args) {
        if (sender instanceof OfflinePlayer) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceHolderAPI")) {
                return PlaceholderAPI.setPlaceholders((OfflinePlayer) sender, text);
            }
        }
        return text;
    }

}

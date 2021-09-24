package org.maxgamer.quickshop.util.language.text.postprocessing.impl;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.util.language.text.postprocessing.PostProcessor;

public class PlaceHolderApiProcessor implements PostProcessor {
    @Override
    public @NotNull String process(@NotNull String text, @Nullable CommandSender sender, String... args) {
        if(sender instanceof OfflinePlayer player) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceHolderAPI")) {
                return PlaceholderAPI.setPlaceholders(player,text);
            }
        }
        return text;
    }
}

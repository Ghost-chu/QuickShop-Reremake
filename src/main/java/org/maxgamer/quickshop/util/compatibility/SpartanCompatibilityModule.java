package org.maxgamer.quickshop.util.compatibility;


import me.vagdedes.spartan.api.API;
import me.vagdedes.spartan.system.Enums;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Util;

public class SpartanCompatibilityModule extends CompatibilityModule {

    public SpartanCompatibilityModule(QuickShop plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "Spartan";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void toggle(@NotNull Player player, boolean checking) {
        if (checking) {
            Util.debugLog(
                    "Calling Spartan ignore "
                            + player.getName()
                            + " cheats detection until we finished permission checks.");

            for (Enums.HackType value : Enums.HackType.values()) {
                API.startCheck(player, value);
            }
        } else {
            Util.debugLog(
                    "Calling Spartan continue follow " + player.getName() + " cheats detection.");
            for (Enums.HackType value : Enums.HackType.values()) {
                API.stopCheck(player, value);
            }
        }
    }
}

package org.maxgamer.quickshop.Util.Compatibility;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

@AllArgsConstructor
public class NCPCompatibilityModule implements CompatibilityModule {
    private QuickShop plugin;
    /**
     * Gets the CompatibilityModule provider name
     *
     * @return Provider name
     */
    @Override
    public @NotNull String getName() {
        return "NoCheatPlus";
    }

    /**
     * Gets the CompatibilityModule provider plugin instance
     *
     * @return Provider Plugin instance
     */
    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    /**
     * Calls CompatibilityModule to toggle the detection status for playerb between on and off
     *
     * @param player The player
     * @param status On or Off
     */
    @Override
    public void toggle(@NotNull Player player, boolean status) {
        if(status){
            Util.debugLog(
                    "Calling NoCheatPlus ignore "
                            + player.getName()
                            + " cheats detection until we finished permission checks.");

            NCPExemptionManager.exemptPermanently(player, CheckType.BLOCKPLACE_REACH);
            NCPExemptionManager.exemptPermanently(player, CheckType.BLOCKPLACE_FASTPLACE);
        }else{
            Util.debugLog(
                    "Calling NoCheatPlus continue follow " + player.getName() + " cheats detection.");
            NCPExemptionManager.unexempt(player, CheckType.BLOCKPLACE_REACH);
            NCPExemptionManager.unexempt(player, CheckType.BLOCKPLACE_FASTPLACE);
        }
    }
}

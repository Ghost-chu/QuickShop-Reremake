package org.maxgamer.quickshop.integration.plotsquared;

import org.bukkit.Bukkit;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.IntegratedPlugin;

public class PlotSquaredIntegrationHolder {
    private static IntegratedPlugin plotSquared;

    public static IntegratedPlugin getPlotSquaredIntegration(QuickShop instance) {
        if (plotSquared == null) {
            if (Bukkit.getPluginManager().getPlugin("PlotSquared").getClass().getPackage().getName().contains("intellectualsite")) {
                plotSquared = new PlotSquaredIntegrationV4(instance);
            } else {
                plotSquared = new PlotSquaredIntegrationV5(instance);
            }
        }
        return plotSquared;
    }
}

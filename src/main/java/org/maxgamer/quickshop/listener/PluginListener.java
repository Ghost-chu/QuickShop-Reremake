package org.maxgamer.quickshop.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.IntegratedPlugin;
import org.maxgamer.quickshop.integration.IntegrationHelper;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.compatibility.CompatibilityManager;

import java.util.Set;

public class PluginListener extends QSListener {

    private static final Set<String> pluginCompatibilityModuleList = CompatibilityManager.getCompatibilityModuleNameMap().keySet();
    private static final Set<String> pluginIntegrationList = IntegrationHelper.getIntegratedPluginNameMap().keySet();
    private final IntegrationHelper integrationHelper = plugin.getIntegrationHelper();
    private final CompatibilityManager compatibilityManager = plugin.getCompatibilityTool();

    public PluginListener(QuickShop plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisabled(PluginDisableEvent event) {
        String pluginName = event.getPlugin().getName();
        if (pluginIntegrationList.contains(pluginName) && plugin.getConfig().getBoolean("integration." + pluginName.toLowerCase() + ".enable")) {
            IntegratedPlugin integratedPlugin = integrationHelper.getIntegrationMap().get(pluginName);
            if (integratedPlugin != null) {
                Util.debugLog("[Hot Load] Calling for unloading " + integratedPlugin.getName());
                integratedPlugin.unload();
                integrationHelper.unregister(integratedPlugin);
            }
        }
        if (pluginCompatibilityModuleList.contains(pluginName)) {
            compatibilityManager.unregister(pluginName);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnabled(PluginEnableEvent event) {
        String pluginName = event.getPlugin().getName();
        if (pluginIntegrationList.contains(pluginName) && plugin.getConfig().getBoolean("integration." + pluginName.toLowerCase() + ".enable")) {
            integrationHelper.register(pluginName);
            IntegratedPlugin integratedPlugin = integrationHelper.getIntegrationMap().get(pluginName);
            if (integratedPlugin != null) {
                Util.debugLog("[Hot Load] Calling for loading " + integratedPlugin.getName());
                integratedPlugin.load();
            }
        }
        if (pluginCompatibilityModuleList.contains(pluginName)) {
            compatibilityManager.register(pluginName);
        }
    }

}

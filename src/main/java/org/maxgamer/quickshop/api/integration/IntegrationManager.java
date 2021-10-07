package org.maxgamer.quickshop.api.integration;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface IntegrationManager {
    /**
     * Getting read-only mapping for all registered modules
     * @return All registered modules
     */
    Map<String, IntegratedPlugin> getIntegrationMap();

    /**
     * Getting all registered Integration modules
     * @return All registered Integration
     */
    List<IntegratedPlugin> getIntegrations();

    /**
     * Re-execute a search task to register available modules if possible
     */
    void searchAndRegisterPlugins();

    /**
     * Register custom integrated module to QuickShop integration system
     *
     * @param integratedPlugin custom integrated module
     */
    void register(@NotNull IntegratedPlugin integratedPlugin);

    /**
     * Register custom integrated module to QuickShop integration system from a class
     *
     * @param integratedPluginClass custom integrated module class
     */
    void register(@NotNull Class<? extends IntegratedPlugin> integratedPluginClass);

    /**
     * Register custom integrated module to QuickShop integration system from a plugin name
     *
     * @param integratedPluginName custom integrated module name
     */
    void register(@NotNull String integratedPluginName);

    /**
     * Check if a class is Integration module
     * @param clazz The class
     * @return Is Integration module
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean isIntegrationClass(@NotNull Class<?> clazz) {
        return clazz.getDeclaredAnnotation(IntegrationStage.class) != null;
    }

    /**
     * Unregister integrated plugin from Integration system
     *
     * @param integratedPluginName plugin name
     */
    void unregister(@NotNull String integratedPluginName);

    /**
     * Unregister all integrated plugin from Integration system
     */
    void unregisterAll();

    /**
     * Unregister integrated plugin from Integration system
     *
     * @param integratedPlugin plugin
     */
    void unregister(@NotNull IntegratedPlugin integratedPlugin);

    /**
     * Check if a integration has been registered
     * @param integrationName The integration
     * @return Registered
     */
    boolean isRegistered(@NotNull String integrationName);
}

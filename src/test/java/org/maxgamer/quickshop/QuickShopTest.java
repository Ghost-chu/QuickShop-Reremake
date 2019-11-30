package org.maxgamer.quickshop;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

public final class QuickShopTest extends JavaPlugin {

    public QuickShopTest() {
        super();
    }

    protected QuickShopTest(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

}

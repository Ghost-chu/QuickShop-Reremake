package org.maxgamer.quickshop;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class QuickShopTest extends JavaPlugin {

    public QuickShopTest() {
        super();
    }

    public QuickShopTest(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description,
                         @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, new File("build"), file);
    }
}

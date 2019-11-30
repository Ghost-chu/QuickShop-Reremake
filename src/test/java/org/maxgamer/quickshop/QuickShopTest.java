package org.maxgamer.quickshop;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.maxgamer.quickshop.File.IFile;
import org.maxgamer.quickshop.File.JSONFile;

import java.io.File;

public final class QuickShopTest extends JavaPlugin {

    private final IFile jsonTest = new JSONFile(
        this,
        new File("messages.json"),
        "messages.json"
    );

    public QuickShopTest() {
        super();
    }

    protected QuickShopTest(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {

    }

}

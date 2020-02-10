package org.maxgamer.quickshop.file;

import io.github.portlek.configs.BukkitManaged;
import io.github.portlek.configs.annotations.Config;
import io.github.portlek.configs.annotations.Value;

@Config(
    name = "config",
    location = "%basedir%/QuickShop"
)
public final class ConfigFile extends BukkitManaged {

    @Value
    public String plugin_prefix = "&6[&eQuickShop]&6";

}

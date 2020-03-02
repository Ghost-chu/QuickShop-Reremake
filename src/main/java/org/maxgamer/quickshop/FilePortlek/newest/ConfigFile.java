package org.maxgamer.quickshop.FilePortlek.newest;

import io.github.portlek.configs.BukkitManaged;
import io.github.portlek.configs.annotations.Config;
import io.github.portlek.configs.annotations.Value;
import io.github.portlek.configs.util.FileType;

@Config(
    name = "config",
    type = FileType.JSON,
    location = "%basedir%/QuickShop"
)
public final class ConfigFile extends BukkitManaged {

    @Value
    public String plugin_language = "en";

}

package org.maxgamer.quickshop.file;

import io.github.portlek.configs.BukkitManaged;
import io.github.portlek.configs.annotations.Config;
import io.github.portlek.configs.annotations.Value;
import io.github.portlek.configs.util.ColorUtil;
import io.github.portlek.configs.util.Replaceable;

@Config(
    name = "config",
    location = "%basedir%/QuickShop"
)
public final class ConfigFile extends BukkitManaged {

    @Value
    public Replaceable<String> plugin_prefix = Replaceable.of("&6[&eQuickShop]&6")
        .map(ColorUtil::colored);

}

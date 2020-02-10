package org.maxgamer.quickshop.file;

import io.github.portlek.configs.BukkitLinkedManaged;
import io.github.portlek.configs.annotations.*;
import io.github.portlek.configs.util.ColorUtil;
import io.github.portlek.configs.util.Replaceable;
import org.jetbrains.annotations.NotNull;
import sun.java2d.loops.FillRect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@LinkedConfig(configs = {
    @Config(
        name = "en",
        location = "%basedir%/QuickShop/languages"
    )
})
public final class LanguageFile extends BukkitLinkedManaged {

    private final Map<String, Supplier<String>> prefix = new HashMap<>();

    @NotNull
    private final ConfigFile configFile;

    public LanguageFile(@NotNull ConfigFile configFile) {
        super(configFile.plugin_language);
        this.configFile = configFile;
    }

    @NotNull
    public Map<String, Supplier<String>> getPrefix() {
        if (!prefix.containsKey("%prefix%")) {
            // Don't replace this lambda with a method reference!
            prefix.put("%prefix%", () -> configFile.plugin_prefix.build());
        }

        return prefix;
    }

    // Values

    @Instance
    public final Generals general = new Generals();

    @Section(path = "general")
    public class Generals {

        @Value
        public Replaceable<String> reload_complete = match(s -> {
            if (s.equals("en")) {
                return Optional.of(
                    Replaceable.of("%prefix% Reload complete! &7Took %ms%ms")
                        .map(ColorUtil::colored)
                        .replace(getPrefix())
                        .replaces("%ms%")
                );
            }

            return Optional.empty();
        });

    }

    @Value
    public Replaceable<List<String>> help_messages = match(s -> {
        if (s.equals("en")) {
            return Optional.of(
                Replaceable.of(
                    "&a====== %prefix% &a======",
                    "&7/qs &r> &eShows help message.",
                    "&7/qs help &r> &eShows help message.",
                    "&7/qs reload &r> &eReloads the plugin."
                )
                    .map(ColorUtil::colored)
                    .replace(getPrefix())
            );
        }

        return Optional.empty();
    });

}

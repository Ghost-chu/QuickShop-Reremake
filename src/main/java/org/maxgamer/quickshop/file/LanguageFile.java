/*
 * MIT License
 *
 * Copyright © 2020 Bukkit Commons Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.maxgamer.quickshop.file;

import io.github.portlek.configs.BukkitLinkedManaged;
import io.github.portlek.configs.annotations.*;
import io.github.portlek.configs.util.ColorUtil;
import io.github.portlek.configs.util.Replaceable;
import org.jetbrains.annotations.NotNull;

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

        @Value
        public Replaceable<String> new_version_found = match(s -> {
            if (s.equals("en")) {
                return Optional.of(
                    Replaceable.of("%prefix% &eNew version found (v%version%)")
                        .map(ColorUtil::colored)
                        .replaces("%version%")
                        .replace(getPrefix())
                );
            }

            return Optional.empty();
        });

        @Value
        public Replaceable<String> latest_version = match(s -> {
            if (s.equals("en")) {
                return Optional.of(
                    Replaceable.of("%prefix% &aYou''re using the latest version (v%version%)")
                        .map(ColorUtil::colored)
                        .replaces("%version%")
                        .replace(getPrefix())
                );
            }

            return Optional.empty();
        });

    }

    @Instance
    public final Errors error = new Errors();

    @Section(path = "error")
    public class Errors {

        @Value
        public Replaceable<String> player_not_found = match(s -> {
            if (s.equals("en")) {
                return Optional.of(
                    Replaceable.of("%prefix% Player not found! (%player_name%)")
                        .map(ColorUtil::colored)
                        .replace(getPrefix())
                        .replaces("%player_name%")
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

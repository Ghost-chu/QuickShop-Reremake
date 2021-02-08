/*
 * This file is a part of project QuickShop, the name is ConfigurationFixer.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;

@AllArgsConstructor
public class ConfigurationFixer {
  private final QuickShop plugin;
  private final FileConfiguration builtInConfig;

  public void fix() {
    // There read the default value as true but we should set default value as
    // false in config.yml So that we can check the configuration may broken or
    // other else.
    if (!plugin.getConfig().getBoolean("config-damaged", true)) {
      return;
    }

    plugin.getLogger().warning(
        "Warning! QuickShop detected the config.yml has been damaged.");
    plugin.getLogger().warning(
        "Backup - Creating backup for file config.yml...");
    try {
      Files.copy(new File(plugin.getDataFolder(), "config.yml").toPath(),
                 new File(plugin.getDataFolder(),
                          "config.yml." + System.currentTimeMillis())
                     .toPath());
    } catch (IOException ioException) {
      plugin.getLogger().log(
          Level.WARNING, "Failed to create config.yml backup.", ioException);
    }
    plugin.getLogger().warning(
        "Fix - Fixing the configuration, this may take a while...");

    for (String key : builtInConfig.getKeys(true)) {
      Object value = plugin.getConfig().get(key);
      Object buildInValue = builtInConfig.get(key);
      if (value == null ||
          !value.getClass().getTypeName().equals(
              Objects.requireNonNull(buildInValue).getClass().getTypeName())) {
        plugin.getLogger().warning("Fixing configuration use default value: " +
                                   key);
        plugin.getConfig().set(key, buildInValue);
      }
    }
    plugin.getLogger().info(
        "QuickShop fixed the damaged parts in configuration that we can found. We recommend you restart the server and make fix apply.");
    plugin.getConfig().set("config-damaged", false);
    plugin.saveConfig();
    plugin.reloadConfig();
  }
}

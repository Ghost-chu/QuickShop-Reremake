package org.maxgamer.quickshop.utils;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class ItemUtils {
  /**
   * Covert YAML string to ItemStack.
   *
   * @param config serialized ItemStack
   * @param forceLoad should let util force load
   * @return ItemStack iStack
   * @throws InvalidConfigurationException when failed deserialize config
   */
  @Nullable
  public static ItemStack deserializeItemStack(@NotNull String config, final boolean forceLoad, final int mode) throws InvalidConfigurationException {
    final DumperOptions yamlOptions = new DumperOptions();
    yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    yamlOptions.setIndent(2);
    final Yaml yaml = new Yaml(yamlOptions);
    final YamlConfiguration yamlConfiguration = new YamlConfiguration();
    final Map<Object, Object> root = yaml.load(config);
    //noinspection unchecked
    final Map<String, Object> item = (Map<String, Object>) root.get("item");
    final int itemDataVersion = Integer.parseInt(String.valueOf(item.get("v")));
    try {
      // Try load the itemDataVersion to do some checks.
      //noinspection deprecation
      if (itemDataVersion > Bukkit.getUnsafe().getDataVersion()) {
        if (forceLoad) {
          if (mode == 0) { // Mode 0
            //noinspection deprecation
            item.put("v", Bukkit.getUnsafe().getDataVersion() - 1);
          } else { // Mode other
            //noinspection deprecation
            item.put("v", Bukkit.getUnsafe().getDataVersion());
          }
          // Okay we have hacked the dataVersion, now put it back
          root.put("item", item);
          config = yaml.dump(root);
        } else {
          //send alert
        }
      }
      yamlConfiguration.loadFromString(config);
      return yamlConfiguration.getItemStack("item");
    } catch (Exception e) {
      e.printStackTrace();
      yamlConfiguration.loadFromString(config);
      return yamlConfiguration.getItemStack("item");
    }
  }
}

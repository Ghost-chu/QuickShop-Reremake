package org.maxgamer.quickshop.localization.resources;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.quickshop.localization.LocalizationType;

import java.util.logging.Level;


@Getter
public class MojangLocalizationResource extends BasicLocalizationResource {

    private final String minecraftLangCode;

    private MojangLocalizationResource(String minecraftLangCode, LocalizationType type, MemoryConfiguration textMap) {
        super(type, textMap);
        this.minecraftLangCode = minecraftLangCode;
    }

    public static MojangLocalizationResource newResource(JavaPlugin plugin, String minecraftLangCode, String content) {
        JsonConfiguration jsonConfiguration = new JsonConfiguration();
        if (content != null) {
            try {
                jsonConfiguration.loadFromString(content);
            } catch (InvalidConfigurationException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load " + minecraftLangCode + " mojang files, i18n may not work", e);
            }
        }
        for (String key : jsonConfiguration.getKeys(true)) {
            if (key.startsWith("block.minecraft.")) {
                jsonConfiguration.set(key.replaceFirst("block\\.minecraft\\.", "item.minecraft."), jsonConfiguration.get(key));
                jsonConfiguration.set(key, null);
            }
        }
        return new MojangLocalizationResource(minecraftLangCode, LocalizationType.OTA, jsonConfiguration);

    }
}

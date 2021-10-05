package org.maxgamer.quickshop.localization.resources;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.localization.LocalizationType;
import org.maxgamer.quickshop.util.language.text.distributions.Distribution;

import java.util.logging.Level;

import static org.maxgamer.quickshop.localization.LocalizationType.OTA;

public class OTALocalizationResource extends BasicLocalizationResource {
    private OTALocalizationResource(LocalizationType type, MemoryConfiguration textMap) {
        super(type, textMap);
    }

    public OTALocalizationResource newResource(QuickShop plugin, Distribution distribution, String path, String langCode) {
        JsonConfiguration configuration = new JsonConfiguration();
        try {
            // Load the locale file from local cache if available
            // Or load the locale file from remote server if it had updates or not exists.
            configuration.loadFromString(distribution.getFile(path, langCode));
        } catch (InvalidConfigurationException exception) {
            try {
                // Force loading the locale file form remote server because file not valid.
                configuration.loadFromString(distribution.getFile(path, langCode, true));
            } catch (InvalidConfigurationException exception1) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load locale file " + langCode + " from remote", exception1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new OTALocalizationResource(OTA, configuration);
    }
}

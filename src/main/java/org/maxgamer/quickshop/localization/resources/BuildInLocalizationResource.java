package org.maxgamer.quickshop.localization.resources;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import org.apache.commons.io.IOUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.localization.LocalizationType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class BuildInLocalizationResource extends BasicLocalizationResource {

    private BuildInLocalizationResource(LocalizationType type, String langCode, MemoryConfiguration textMap, BasicLocalizationResource fallback) {
        super(type, textMap);
    }

    public static BuildInLocalizationResource newResource(QuickShop plugin, String fileName) {
        JsonConfiguration bundledLang = new JsonConfiguration();
        try (InputStream stream = plugin.getResource("lang-original/" + fileName)) {
            if (stream == null) {
                logger.log(Level.WARNING, "Unable to find build-in language file, some strings may missing!");
            } else {
                bundledLang.loadFromString(new String(IOUtils.toByteArray(new BufferedInputStream(stream)), StandardCharsets.UTF_8));
            }
        } catch (IOException | InvalidConfigurationException ex) {
            logger.log(Level.SEVERE, "Cannot load bundled language file from Jar, some strings may missing!", ex);
        }
        return new BuildInLocalizationResource(LocalizationType.LOCAL, "en_us", bundledLang, null);
    }
}

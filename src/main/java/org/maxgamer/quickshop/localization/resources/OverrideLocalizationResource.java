package org.maxgamer.quickshop.localization.resources;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import org.bukkit.configuration.MemoryConfiguration;
import org.maxgamer.quickshop.localization.LocalizationType;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class OverrideLocalizationResource extends BasicLocalizationResource {
    private final File file;

    private OverrideLocalizationResource(LocalizationType type, MemoryConfiguration textMap, File file) {
        super(type, textMap);
        this.file = file;
    }

    public static OverrideLocalizationResource newResource(File file) {
        if (file.exists()) {
            return new OverrideLocalizationResource(LocalizationType.OVERRIDE, JsonConfiguration.loadConfiguration(file), file);
        } else {
            return new OverrideLocalizationResource(LocalizationType.OVERRIDE, new JsonConfiguration(), file);
        }
    }

    public void save() {
        try {

            ((JsonConfiguration) textMap).save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save override file", e);
        }
    }
}

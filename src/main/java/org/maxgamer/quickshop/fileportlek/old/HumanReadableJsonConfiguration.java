package org.maxgamer.quickshop.fileportlek.old;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.dumptruckman.bukkit.configuration.util.SerializationHelper;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HumanReadableJsonConfiguration extends JsonConfiguration {

    private static final Logger logger = Logger.getLogger(HumanReadableJsonConfiguration.class.getName());

    public static HumanReadableJsonConfiguration loadConfiguration(@NotNull File file) {
        HumanReadableJsonConfiguration configuration = new HumanReadableJsonConfiguration();
        try {
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException var4) {
            logger.log(Level.SEVERE, "Cannot load json file " + file, var4);
        }
        return configuration;
    }

    @Override
    public @NotNull String saveToString() {
        return JsonUtil.getHumanReadableGson().toJson(SerializationHelper.serialize(this.getValues(false)));
    }
}

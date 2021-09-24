package org.maxgamer.quickshop.util.language.text;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.language.text.distributions.Distribution;
import org.maxgamer.quickshop.util.language.text.distributions.crowdin.CrowdinOTA;
import org.maxgamer.quickshop.util.language.text.postprocessing.PostProcessor;
import org.maxgamer.quickshop.util.language.text.postprocessing.impl.ColorProcessor;
import org.maxgamer.quickshop.util.language.text.postprocessing.impl.FillerProcessor;
import org.maxgamer.quickshop.util.language.text.postprocessing.impl.PlaceHolderApiProcessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TextManager {
    private final QuickShop plugin;
    private final Distribution distribution;
    private final File overrideFilesFolder;
    // <File <Locale, Section>>
    private final Map<String, Map<String, JsonConfiguration>> locale2ContentMapping = new HashMap<>();
    private final static String languageFileCrowdin = "/master/src/main/resources/lang/%locale%/messages.json";
    public List<PostProcessor> postProcessors = new ArrayList<>();
    private JsonConfiguration bundledLang = new JsonConfiguration();

    public TextManager(QuickShop plugin) {
        this.plugin = plugin;
        this.distribution = new CrowdinOTA(plugin);
        this.overrideFilesFolder = new File(plugin.getDataFolder(), "lang-override");
        load();
    }

    public void load() {
        locale2ContentMapping.clear();
        postProcessors.clear();
        // Load mapping
        //for (String availableFile : distribution.getAvailableFiles()) {
        try (InputStream stream = plugin.getResource("lang-original/messages.json")) {
            if (stream != null)
                bundledLang.loadFromString(new String(Util.inputStream2ByteArray(stream), StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException ignored) {
            bundledLang = new JsonConfiguration();
        }
        for (String availableLanguage : distribution.getAvailableLanguages()) {
            try {
                // load OTA text
                String localeFileContent = distribution.getFile(languageFileCrowdin, availableLanguage);
                Map<String, JsonConfiguration> fileLocaleMapping = locale2ContentMapping.computeIfAbsent(languageFileCrowdin, k -> new HashMap<>());
                JsonConfiguration configuration = new JsonConfiguration();
                fileLocaleMapping.put(availableLanguage, configuration);
                configuration.loadFromString(localeFileContent);
                // load override text
                JsonConfiguration override = new JsonConfiguration();
                File localOverrideFile = new File(overrideFilesFolder, availableLanguage + ".json");
                if (localOverrideFile.exists()) {
                    override.loadFromString(Files.readString(localOverrideFile.toPath()));
                    for (String key : override.getKeys(true)) {
                        configuration.set(key, override.get(key));
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Couldn't update the translation for locale " + availableLanguage, e);
            }
        }
        postProcessors.add(new FillerProcessor());
        postProcessors.add(new ColorProcessor());
        postProcessors.add(new PlaceHolderApiProcessor());
    }

    public Text of(CommandSender sender, String path, String... args) {
        return new Text(this, sender, locale2ContentMapping.get(languageFileCrowdin), path, args);
    }

    static class Text {
        private final TextManager manager;
        private final String path;
        private final QuickShop plugin;
        private final Map<String, JsonConfiguration> mapping;
        private final CommandSender sender;
        private final String[] args;

        private Text(TextManager manager, CommandSender sender, Map<String, JsonConfiguration> mapping, String path, String... args) {
            this.plugin = manager.plugin;
            this.manager = manager;
            this.sender = sender;
            this.mapping = mapping;
            this.path = path;
            this.args = args;
        }

        @Nullable
        private String fallbackLocal() {
            return manager.bundledLang.getString(path);
        }

        @NotNull
        private String postProcess(@NotNull String text) {
            for (PostProcessor postProcessor : this.manager.postProcessors)
                text = postProcessor.process(text, sender, args);
            return text;
        }

        @NotNull
        public String forLocale(@NotNull String locale) {
            JsonConfiguration index = mapping.get(locale);
            if (index == null && locale.equals("en")) {
                String str = fallbackLocal();
                if (str == null)
                    return "Fallback Missing Language Key: " + path + ", report to QuickShop!";
            }
            if (index == null)
                return forLocale("en");
            String str = index.getString(locale);
            if (str == null)
                return "Missing Language Key: " + path;
            return postProcess(str);
        }

        @NotNull
        public String forLocale(@NotNull Player player){
            return forLocale(player.getLocale());
        }
    }
}

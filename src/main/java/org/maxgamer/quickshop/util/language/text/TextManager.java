package org.maxgamer.quickshop.util.language.text;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.language.text.distributions.Distribution;
import org.maxgamer.quickshop.util.language.text.distributions.crowdin.CrowdinOTA;
import org.maxgamer.quickshop.util.language.text.postprocessing.PostProcessor;
import org.maxgamer.quickshop.util.language.text.postprocessing.impl.ColorProcessor;
import org.maxgamer.quickshop.util.language.text.postprocessing.impl.FillerProcessor;
import org.maxgamer.quickshop.util.language.text.postprocessing.impl.PlaceHolderApiProcessor;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public class TextManager implements Reloadable {
    private final QuickShop plugin;
    private final Distribution distribution;
    // <File <Locale, Section>>
    private final Map<String, Map<String, JsonConfiguration>> locale2ContentMapping = new HashMap<>();
    private final Map<String, JsonConfiguration> bundledFile2ContentMapping = new HashMap<>();
    private final static String CROWDIN_LANGUAGE_FILE = "/master/src/main/resources/lang/%locale%/messages.json";
    public final List<PostProcessor> postProcessors = new ArrayList<>();
    private List<String> disabledLanguages = new ArrayList<>();


    public TextManager(QuickShop plugin) {
        this.plugin = plugin;
        plugin.getReloadManager().register(this);
        this.distribution = new CrowdinOTA(plugin);
        load();

    }

    /**
     * Generate the override files storage path
     *
     * @param crowdinPath The crowdin file path
     * @return Override files storage path
     */
    @NotNull
    private File getOverrideFilesFolder(@NotNull String crowdinPath) {
        File file = new File(crowdinPath);
        File folder = new File(new File(plugin.getDataFolder(), "overrides"), file.getName() + ".overrides");
        folder.mkdirs();
        return folder;
    }

    /**
     * Reset everything
     */
    private void reset() {
        locale2ContentMapping.clear();
        postProcessors.clear();
        bundledFile2ContentMapping.clear();
        disabledLanguages.clear();
    }

    /**
     * Loading bundled files from Jar file
     *
     * @param file The Crowdin file path
     * @return The bundled file configuration object
     */
    private JsonConfiguration loadBundled(String file) {
        JsonConfiguration bundledLang = new JsonConfiguration();
        try {
            File fileObject = new File(file);
            bundledLang.loadFromString(new String(IOUtils.toByteArray(new InputStreamReader(plugin.getResource("lang-original/" + fileObject.getName())), StandardCharsets.UTF_8)));
        } catch (IOException | InvalidConfigurationException ex) {
            bundledLang = new JsonConfiguration();
            plugin.getLogger().log(Level.SEVERE, "Cannot load bundled language file from Jar, some strings may missing!", ex);
        }
        return bundledLang;
    }

    /**
     * Loading Crowdin OTA module and i18n system
     */
    public void load() {
        plugin.getLogger().info("Checking for translation updates...");
        this.reset();
        disabledLanguages = plugin.getConfig().getStringList("disabled-languages");
        // Initial file mapping
        locale2ContentMapping.computeIfAbsent(CROWDIN_LANGUAGE_FILE, e -> new HashMap<>()); // Prevent nullportinter exception
        distribution.getAvailableFiles().forEach(file -> locale2ContentMapping.computeIfAbsent(file, e -> new HashMap<>()));

        // Read bundled language files
        distribution.getAvailableFiles().forEach(crowdinFile -> this.bundledFile2ContentMapping.computeIfAbsent(crowdinFile, e -> loadBundled(crowdinFile)));

        // Multi File and Multi-Language loader
        distribution.getAvailableLanguages().parallelStream().forEach(crowdinCode -> distribution.getAvailableFiles().parallelStream().forEach(crowdinFile -> {
            try {
                // Minecraft client use lowercase wi
                String minecraftCode = crowdinCode.toLowerCase(Locale.ROOT).replace("-", "_");
                if (disabledLanguages.contains(minecraftCode) || disabledLanguages.contains(crowdinCode)) {
                    Util.debugLog("Locale " + crowdinCode + "(" + minecraftCode + ") has been disabled, skipping.");
                    return;
                }
                Util.debugLog("Loading translation for locale: " + crowdinCode + " (" + minecraftCode + ")");
                JsonConfiguration configuration = new JsonConfiguration();
                try {
                    // Load the locale file from local cache if available
                    // Or load the locale file from remote server if it had updates or not exists.
                    configuration.loadFromString(distribution.getFile(crowdinFile, crowdinCode));
                } catch (InvalidConfigurationException exception) {
                    // Force loading the locale file form remote server because file not valid.
                    configuration.loadFromString(distribution.getFile(crowdinFile, crowdinCode, true));
                }
                // Loading override text (allow user modification the translation)
                JsonConfiguration override = new JsonConfiguration();
                File localOverrideFile = new File(getOverrideFilesFolder(crowdinFile), minecraftCode + ".json");
                if (!localOverrideFile.exists()) {
                    localOverrideFile.getParentFile().mkdirs();
                    localOverrideFile.createNewFile();
                }
                override.loadFromString(Util.readToString(localOverrideFile));
                // Prevent user override important keys
                for (String key : override.getKeys(true)) {
                    if ("language-version".equals(key) || "config-version".equals(key) || "version".equals(key)) {
                        continue;
                    }
                    configuration.set(key, override.get(key));
                }
                locale2ContentMapping.get(crowdinFile).computeIfAbsent(minecraftCode, e -> configuration);
                Util.debugLog("Locale " + crowdinFile.replace("%locale%", crowdinCode) + " has been successfully loaded");
            } catch (CrowdinOTA.OTAException e) {
                // Key founds in available locales but not in custom mapping on crowdin platform
                plugin.getLogger().warning("Couldn't update the translation for locale " + crowdinCode + " because it not configured, please report to QuickShop");
            } catch (IOException e) {
                // Network error
                plugin.getLogger().log(Level.WARNING, "Couldn't update the translation for locale " + crowdinCode + " please check your network connection.", e);
            } catch (Exception e) {
                // Translation syntax error or other exceptions
                plugin.getLogger().log(Level.WARNING, "Couldn't update the translation for locale " + crowdinCode + ".", e);
            }
        }));

        // Register post processor
        postProcessors.add(new FillerProcessor());
        postProcessors.add(new PlaceHolderApiProcessor());
        postProcessors.add(new ColorProcessor());
    }

    /**
     * Getting the translation with path with default locale
     *
     * @param path THe path
     * @param args The arguments
     * @return The text object
     */
    public Text of(@NotNull String path, String... args) {
        return new Text(this, (CommandSender) null, locale2ContentMapping.get(CROWDIN_LANGUAGE_FILE), bundledFile2ContentMapping.get(CROWDIN_LANGUAGE_FILE), path, args);
    }

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The sender
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    public Text of(@Nullable CommandSender sender, @NotNull String path, String... args) {
        return new Text(this, sender, locale2ContentMapping.get(CROWDIN_LANGUAGE_FILE), bundledFile2ContentMapping.get(CROWDIN_LANGUAGE_FILE), path, args);
    }

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The player unique id
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    public Text of(@Nullable UUID sender, @NotNull String path, String... args) {
        return new Text(this, sender, locale2ContentMapping.get(CROWDIN_LANGUAGE_FILE), bundledFile2ContentMapping.get(CROWDIN_LANGUAGE_FILE), path, args);
    }

    /**
     * Getting the translation with path with default locale (if available)
     *
     * @param path The path
     * @param args The arguments
     * @return The text object
     */
    public TextList ofList(@NotNull String path, String... args) {
        return new TextList(this, (CommandSender) null, locale2ContentMapping.get(CROWDIN_LANGUAGE_FILE), bundledFile2ContentMapping.get(CROWDIN_LANGUAGE_FILE), path, args);
    }

    /**
     * Getting the translation with path  with player's locale (if available)
     *
     * @param sender The player unique id
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    public TextList ofList(@Nullable UUID sender, @NotNull String path, String... args) {
        return new TextList(this, sender, locale2ContentMapping.get(CROWDIN_LANGUAGE_FILE), bundledFile2ContentMapping.get(CROWDIN_LANGUAGE_FILE), path, args);
    }

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The player
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    public TextList ofList(@Nullable CommandSender sender, @NotNull String path, String... args) {
        return new TextList(this, sender, locale2ContentMapping.get(CROWDIN_LANGUAGE_FILE), bundledFile2ContentMapping.get(CROWDIN_LANGUAGE_FILE), path, args);
    }

    public static class TextList {
        private final TextManager manager;
        private final String path;
        private final QuickShop plugin;
        private final Map<String, JsonConfiguration> mapping;
        private final CommandSender sender;
        private final String[] args;
        private final JsonConfiguration bundled;

        private TextList(TextManager manager, CommandSender sender, Map<String, JsonConfiguration> mapping, JsonConfiguration bundled, String path, String... args) {
            this.plugin = manager.plugin;
            this.manager = manager;
            this.sender = sender;
            this.mapping = mapping;
            this.bundled = bundled;
            this.path = path;
            this.args = args;
        }

        private TextList(TextManager manager, UUID sender, Map<String, JsonConfiguration> mapping, JsonConfiguration bundled, String path, String... args) {
            this.plugin = manager.plugin;
            this.manager = manager;
            if (sender != null) {
                this.sender = Bukkit.getPlayer(sender);
            } else {
                this.sender = null;
            }
            this.mapping = mapping;
            this.bundled = bundled;
            this.path = path;
            this.args = args;
        }

        /**
         * Getting the bundled fallback text
         *
         * @return The bundled text
         */
        private @NotNull List<String> fallbackLocal() {
            return this.bundled.getStringList(path);
        }

        /**
         * Post processes the text
         *
         * @param text The text
         * @return The text that processed
         */
        @NotNull
        private List<String> postProcess(@NotNull List<String> text) {
            List<String> texts = new ArrayList<>();
            for (PostProcessor postProcessor : this.manager.postProcessors) {
                for (String s : text) {
                    texts.add(postProcessor.process(s, sender, args));
                }
            }
            return texts;
        }

        /**
         * Getting the text that use specify locale
         *
         * @param locale The minecraft locale code (like en_us)
         * @return The text
         */
        @NotNull
        public List<String> forLocale(@NotNull String locale) {
            JsonConfiguration index = mapping.get(locale);
            if (index == null) {
                if ("en_us".equals(locale)) {
                    List<String> str = fallbackLocal();
                    if (str.isEmpty()) {
                        return Collections.singletonList("Fallback Missing Language Key: " + path + ", report to QuickShop!");
                    }
                    return postProcess(str);
                } else {
                    return forLocale("en_us");
                }
            } else {
                List<String> str = index.getStringList(path);
                if (str.isEmpty()) {
                    return Collections.singletonList("Missing Language Key: " + path);
                } else {
                    return postProcess(str);
                }
            }

        }

        /**
         * Getting the text for player locale
         *
         * @return Getting the text for player locale
         */
        @NotNull
        public List<String> forLocale() {
            if (sender instanceof Player) {
                return forLocale(((Player) sender).getLocale());
            } else {
                return forLocale("en_us");
            }
        }

        /**
         * Send text to the player
         */
        public void send() {
            if (sender == null) {
                return;
            }
            for (String s : forLocale()) {
                MsgUtil.sendDirectMessage(sender, s);
            }
        }
    }

    public static class Text {
        private final TextManager manager;
        private final String path;
        private final QuickShop plugin;
        private final Map<String, JsonConfiguration> mapping;
        private final CommandSender sender;
        private final String[] args;
        private final JsonConfiguration bundled;

        private Text(TextManager manager, CommandSender sender, Map<String, JsonConfiguration> mapping, JsonConfiguration bundled, String path, String... args) {
            this.plugin = manager.plugin;
            this.manager = manager;
            this.sender = sender;
            this.mapping = mapping;
            this.path = path;
            this.bundled = bundled;
            this.args = args;
        }

        private Text(TextManager manager, UUID sender, Map<String, JsonConfiguration> mapping, JsonConfiguration bundled, String path, String... args) {
            this.plugin = manager.plugin;
            this.manager = manager;
            if (sender != null) {
                this.sender = Bukkit.getPlayer(sender);
            } else {
                this.sender = null;
            }
            this.mapping = mapping;
            this.bundled = bundled;
            this.path = path;
            this.args = args;
        }

        /**
         * Getting the bundled fallback text
         *
         * @return The bundled text
         */
        @Nullable
        private String fallbackLocal() {
            return this.bundled.getString(path);
        }

        /**
         * Post processes the text
         *
         * @param text The text
         * @return The text that processed
         */
        @NotNull
        private String postProcess(@NotNull String text) {
            for (PostProcessor postProcessor : this.manager.postProcessors) {
                text = postProcessor.process(text, sender, args);
            }
            return text;
        }

        /**
         * Getting the text that use specify locale
         *
         * @param locale The minecraft locale code (like en_us)
         * @return The text
         */
        @NotNull
        public String forLocale(@NotNull String locale) {
            JsonConfiguration index = mapping.get(locale);
            if (index == null) {
                if ("en_us".equals(locale)) {
                    String str = fallbackLocal();
                    if (str == null) {
                        return "Fallback Missing Language Key: " + path + ", report to QuickShop!";
                    }
                    return postProcess(str);
                } else {
                    return forLocale("en_us");
                }
            } else {
                String str = index.getString(path);
                if (str == null) {
                    return "Missing Language Key: " + path;
                }
                return postProcess(str);
            }
        }

        /**
         * Getting the text for player locale
         *
         * @return Getting the text for player locale
         */
        @NotNull
        public String forLocale() {
            if (sender instanceof Player) {
                return forLocale(((Player) sender).getLocale());
            } else {
                return forLocale("en_us");
            }
        }

        /**
         * Send text to the player
         */
        public void send() {
            if (sender == null) {
                return;
            }
            String lang = forLocale();
            MsgUtil.sendDirectMessage(sender, lang);
            // plugin.getQuickChat().send(sender, lang);
        }
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        this.load();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}

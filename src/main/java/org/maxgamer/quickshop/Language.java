package org.maxgamer.quickshop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Language {
    private QuickShop plugin;
    //private List<String> languages = new ArrayList<>();

    Language(QuickShop plugin) {
        this.plugin = plugin;

    }

    /**
     * Get target language's type file.
     *
     * @param language The target language
     * @param type     The file type for you want get. e.g. messages
     * @return The target file's InputStream.
     */
    public InputStream getFile(@Nullable String language, @Nullable String type) {
        if (language == null) {
            language = "en";
            Util.debugLog("Using the default language (EN) cause language is null.");
        }
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        InputStream inputStream = plugin.getResource(type + "-" + language + ".yml");
        if (inputStream == null) {
            Util.debugLog("Using the default language because we can't get the InputStream.");
            inputStream = plugin.getResource(type + "-" + "en" + ".yml");
        }
        return inputStream;
        //File name should call    type-language.yml    ---> config-zh.yml
    }

    //Write file under plugin folder

    /**
     * Save the target language's type file to the datafolder
     *
     * @param language Target language
     * @param type     Target type
     * @param fileName The filename you want write to the plugin datafolder.
     */
    public void saveFile(@NotNull String language, @NotNull String type, @NotNull String fileName) {
        File targetFile = new File(plugin.getDataFolder(), fileName);
        if (!targetFile.exists()) {
            try {
                targetFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            InputStream is = getFile(language, type);
            FileOutputStream fos = new FileOutputStream(targetFile);
            byte[] b = new byte[1024];
            int length;
            while ((length = is.read(b)) != -1) {
                fos.write(b, 0, length);
            }
            is.close();
            fos.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}

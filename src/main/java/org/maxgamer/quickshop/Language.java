package org.maxgamer.quickshop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Util.Util;

public class Language {
    private QuickShop plugin;
    //private List<String> languages = new ArrayList<>();

    Language(QuickShop plugin) {
        this.plugin = plugin;
        // languages.clear();
        // languages.add("en");
        // languages.add("fr");
        // languages.add("de");
        // languages.add("ko");
        // languages.add("pl");
        // languages.add("ru");
        // languages.add("sv");
        // languages.add("zh_TW");
    }

    // public String getComputerLanguage() {
    // 	if(plugin.getConfig().getString("language")==null || plugin.getConfig().getString("language").equals("default")) {//Allow user replace this
    // 		return Locale.getDefault().getLanguage().toString();
    // 	}
    // 	return plugin.getConfig().getString("language");
    // }
    //public List<String> getSupportsLanguageList() {
    //    return languages;
    //}

    /**
     * Get target language's type file.
     *
     * @param language The target language
     * @param type     The file type for you want get. e.g. messages
     * @return The target file's InputStream.
     */
    public InputStream getFile(@Nullable String language, @Nullable String type) {
        if ((language == null)) {
            language = "en";
            Util.debugLog("Use default language cause language is null.");
        }
        if (type == null || type.isEmpty())
            throw new IllegalArgumentException("Type cannot be null or empty");
        InputStream inputStream = plugin.getResource(type + "-" + language + ".yml");
        if (inputStream == null) {
            Util.debugLog("Use default language cause can't get InputStream.");
            plugin.getResource(type + "-" + "en" + ".yml");
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
        File targetFile = new File(plugin.getDataFolder()
                .toPath() + "/" + fileName);
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

        // InputStream is = getFile(language, type);
        // if (is == null)
        //     return;
        // String messagei18nYaml2 = new String(Util.inputStream2ByteArray(is));
        // try {
        //     is.close();
        // } catch (IOException e) {
        //     //Ignore
        // }
        // // YamlIsSucked yamlIsSucked = new YamlIsSucked();
        // // String messagei18nJson = yamlIsSucked.readYaml2ToJson(messagei18nYaml2);
        // // String messagei18nYaml1 = yamlIsSucked.writeJson2Yaml1(messagei18nJson);
        // InputStream inputStream = new ByteArrayInputStream(messagei18nYaml1.getBytes());
        // YamlConfiguration messagei18nYAML = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
        // //File name should call    type-language.yml    ---> config-zh.yml
        // String finalYaml1FileContect = messagei18nYAML.saveToString();
        //
        // InputStream finalYaml1FileInputStream = new ByteArrayInputStream(finalYaml1FileContect.getBytes());
        // try {
        //     FileOutputStream fos = new FileOutputStream(targetFile);
        //
        //     byte[] b = new byte[1024];
        //     int length;
        //     while ((length = finalYaml1FileInputStream.read(b)) != -1) {
        //         fos.write(b, 0, length);
        //     }
        //     fos.close();
        //
        // } catch (IOException e) {
        //     //Ignore
        // }


    }
}

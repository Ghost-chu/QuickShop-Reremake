package org.maxgamer.quickshop.util.language.text;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TextMapper {
    private final Map<String, Map<String, JsonConfiguration>> locale2ContentMapping = new HashMap<>();
    private final Map<String, JsonConfiguration> bundledFile2ContentMapping = new HashMap<>();

    /**
     * Reset TextMapper
     */
    public void reset(){
        this.locale2ContentMapping.clear();
        this.bundledFile2ContentMapping.clear();
    }

    /**
     * Deploy new locale to TextMapper with cloud values and bundle values
     * @param distributionPath Distribution Path
     * @param locale The locale code
     * @param distribution The values from Distribution platform
     * @param bundled The values from bundled file
     */
    public void deploy(@NotNull String distributionPath, @NotNull String locale, @NotNull JsonConfiguration distribution, @NotNull JsonConfiguration bundled){
        this.bundledFile2ContentMapping.put(distributionPath,bundled);
        this.locale2ContentMapping.computeIfAbsent(distributionPath,e->new HashMap<>());
        this.locale2ContentMapping.get(distributionPath).put(locale,distribution);
    }

    /**
     * Deploy bundled file
     * @param distributionPath Distribution Path
     * @param bundled The values from bundled file
     */
    public void deployBundled(@NotNull String distributionPath, @NotNull JsonConfiguration bundled){
        this.bundledFile2ContentMapping.put(distributionPath,bundled);
    }

    /**
     * Remove all locales data under specific distribution path
     * @param distributionPath The distribution path
     */
    public void remove(@NotNull String distributionPath){
        this.bundledFile2ContentMapping.remove(distributionPath);
        this.locale2ContentMapping.remove(distributionPath);
    }
    /**
     * Remove specific locales data under specific distribution path
     * @param distributionPath The distribution path
     * @param locale The locale
     */
    public void remove(@NotNull String distributionPath, @NotNull String locale){
        if(this.locale2ContentMapping.containsKey(distributionPath)){
            this.locale2ContentMapping.get(distributionPath).remove(locale);
        }
    }

    /**
     * Remove specific bundled data
     * @param distributionPath The distribution path
     */
    public void removeBundled(@NotNull String distributionPath){
        this.bundledFile2ContentMapping.remove(distributionPath);
    }

    /**
     * Getting specific bundled data
     * @param distributionPath The distribution path
     * @return The bundled data, null if never deployed
     */
    public @Nullable JsonConfiguration getBundled(@NotNull String distributionPath){
        return this.bundledFile2ContentMapping.get(distributionPath);

    }
    /**
     * Getting locales data under specific distribution data
     * @param distributionPath The distribution path
     * @return The locales data, empty if never deployed
     */
    public @NotNull Map<String,JsonConfiguration> getDistribution(@NotNull String distributionPath){
        return this.locale2ContentMapping.getOrDefault(distributionPath, Collections.emptyMap());
    }
    /**
     * Getting specific locale data under specific distribution data
     * @param distributionPath The distribution path
     * @param locale The specific locale
     * @return The locale data, null if never deployed
     */
    public @Nullable JsonConfiguration getDistribution(@NotNull String distributionPath, @NotNull String locale){
        return this.getDistribution(distributionPath).get(locale);
    }

}

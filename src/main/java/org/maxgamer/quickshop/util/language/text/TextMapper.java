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

    public void reset(){
        this.locale2ContentMapping.clear();
        this.bundledFile2ContentMapping.clear();
    }

    public void deploy(@NotNull String distributionPath, @NotNull String locale, @NotNull JsonConfiguration distribution, @NotNull JsonConfiguration bundled){
        this.bundledFile2ContentMapping.put(distributionPath,bundled);
        this.locale2ContentMapping.computeIfAbsent(distributionPath,e->new HashMap<>());
        this.locale2ContentMapping.get(distributionPath).put(locale,distribution);
    }

    public void remove(@NotNull String distributionPath){
        this.bundledFile2ContentMapping.remove(distributionPath);
        this.locale2ContentMapping.remove(distributionPath);
    }

    public void remove(@NotNull String distributionPath, @NotNull String locale){
        if(this.locale2ContentMapping.containsKey(distributionPath)){
            this.locale2ContentMapping.get(distributionPath).remove(locale);
        }
    }

    public void removeBundled(@NotNull String distributionPath){
        this.bundledFile2ContentMapping.remove(distributionPath);
    }


    public @Nullable JsonConfiguration getBundled(@NotNull String distributionPath){
        return this.bundledFile2ContentMapping.get(distributionPath);
    }

    public @NotNull Map<String,JsonConfiguration> getDistribution(@NotNull String distributionPath){
        return this.locale2ContentMapping.getOrDefault(distributionPath, Collections.emptyMap());
    }

    public @Nullable JsonConfiguration getDistribution(@NotNull String distributionPath, @NotNull String locale){
        return this.getDistribution(distributionPath).get(locale);
    }

}

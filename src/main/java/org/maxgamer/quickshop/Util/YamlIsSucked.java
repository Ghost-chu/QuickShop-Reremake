package org.maxgamer.quickshop.Util;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import org.snakeyaml.engine.v1.api.Load;
import org.snakeyaml.engine.v1.api.LoadSettings;
import org.snakeyaml.engine.v1.api.LoadSettingsBuilder;

/**
 * A Util class to convert YAML1.2 to YAML1.0
 */
public class YamlIsSucked {
    /**
     * Convert YAML1.2 config to Json
     *
     * @param yaml2 Target yaml config
     * @return Json string
     */
    public String readYaml2ToJson(String yaml2) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) load.loadFromString(yaml2);
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    /**
     * Convert Json to YAML1.0 config
     * @param json Target Json string
     * @return YAML 1.0 config
     */
    public String writeJson2Yaml1(String json) {
        try {
            JsonNode jsonNodeTree = new ObjectMapper().readTree(json);
            return new YAMLMapper().writeValueAsString(jsonNodeTree);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }
}

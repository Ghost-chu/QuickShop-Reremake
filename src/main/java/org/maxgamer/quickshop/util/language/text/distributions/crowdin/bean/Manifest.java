package org.maxgamer.quickshop.util.language.text.distributions.crowdin.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class Manifest {

    @JsonProperty("files")
    private List<String> files;
    @JsonProperty("languages")
    private List<String> languages;
    @JsonProperty("language_mapping")
    private List<?> languageMapping;
    @JsonProperty("custom_languages")
    private List<?> customLanguages;
    @JsonProperty("timestamp")
    private Integer timestamp;
}

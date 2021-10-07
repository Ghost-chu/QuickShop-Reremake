package org.maxgamer.quickshop.localization.text.distributions.crowdin.bean;

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
    @JsonProperty("custom_languages")
    private List<?> customLanguages;
    @JsonProperty("timestamp")
    private Integer timestamp;

}

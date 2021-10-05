package org.maxgamer.quickshop.util.language.text.distributions.crowdin.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@Data
public class Manifest {

    @SerializedName("files")
    private List<String> files;
    @SerializedName("languages")
    private List<String> languages;
    @SerializedName("custom_languages")
    private List<?> customLanguages;
    @SerializedName("timestamp")
    private Integer timestamp;

}

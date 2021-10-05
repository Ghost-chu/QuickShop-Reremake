package org.maxgamer.quickshop.localization.distributions.mojang.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class VersionManifest {

    @SerializedName("latest")
    private LatestDTO latest;
    @SerializedName("versions")
    private List<VersionsDTO> versions;

    @NoArgsConstructor
    @Data
    public static class LatestDTO {
        @SerializedName("release")
        private String release;
        @SerializedName("snapshot")
        private String snapshot;
    }

    @NoArgsConstructor
    @Data
    public static class VersionsDTO {
        @SerializedName("id")
        private String id;
        @SerializedName("type")
        private String type;
        @SerializedName("url")
        private String url;
        @SerializedName("time")
        private String time;
        @SerializedName("releaseTime")
        private String releaseTime;
    }
}

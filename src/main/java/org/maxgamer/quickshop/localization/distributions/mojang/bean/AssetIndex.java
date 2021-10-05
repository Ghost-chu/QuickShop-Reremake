package org.maxgamer.quickshop.localization.distributions.mojang.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.Map;

public class AssetIndex {

    @Getter
    @SerializedName("objects")
    private Map<String, Meta> objects;

    public static class Meta {
        //"hash": "bdf48ef6b5d0d23bbb02e17d04865216179f510a", "size": 3665
        @Getter
        @SerializedName("hash")
        String hash;
        @Getter
        @SerializedName("size")
        int size;
    }
}
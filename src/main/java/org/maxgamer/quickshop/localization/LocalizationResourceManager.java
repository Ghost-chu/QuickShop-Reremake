package org.maxgamer.quickshop.localization;

import org.maxgamer.quickshop.util.language.text.distributions.Distribution;

import java.io.File;

public class LocalizationResourceManager {

    private final String fileName;
    private final String resourcesPath;
    private final Distribution distribution;

    LocalizationResourceManager(Distribution distribution, String resourcesPath) {
        this.distribution = distribution;
        this.resourcesPath = resourcesPath;
        this.fileName = new File(resourcesPath).getName();
    }

}

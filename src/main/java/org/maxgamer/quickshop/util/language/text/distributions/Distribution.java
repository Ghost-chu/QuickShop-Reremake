package org.maxgamer.quickshop.util.language.text.distributions;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Distribution {
    @NotNull List<String> getAvailableLanguages();

    @NotNull List<String> getAvailableFiles();

    @NotNull String getFile(String fileCrowdinPath, String crowdinLocale) throws Exception;

    @NotNull String getFile(String fileCrowdinPath, String crowdinLocale, boolean forceFlush) throws Exception;
}

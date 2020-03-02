package org.maxgamer.quickshop.FilePortlek.newest;

import io.github.portlek.configs.BukkitLinkedManaged;
import io.github.portlek.configs.annotations.Config;
import io.github.portlek.configs.annotations.LinkedConfig;
import io.github.portlek.configs.util.FileType;
import org.jetbrains.annotations.NotNull;

@LinkedConfig(configs = {
    @Config(
        name = "de",
        location = "%basedir%/QuickShop/messages",
        copyDefault = true,
        type = FileType.JSON,
        resourcePath = "messages"
    ),
    @Config(
        name = "en",
        location = "%basedir%/QuickShop/messages",
        copyDefault = true,
        type = FileType.JSON,
        resourcePath = "messages"
    ),
    @Config(
        name = "en-US",
        location = "%basedir%/QuickShop/messages",
        copyDefault = true,
        type = FileType.JSON,
        resourcePath = "messages"
    )
})
public final class LanguageFile extends BukkitLinkedManaged {

    public LanguageFile(@NotNull final String language) {
        super(language);
    }

}

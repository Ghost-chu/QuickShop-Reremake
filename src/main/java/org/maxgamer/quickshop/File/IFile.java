package org.maxgamer.quickshop.File;

import org.jetbrains.annotations.NotNull;

public interface IFile {

    void create();

    void reload();

    @NotNull
    String getMessage(@NotNull String path, @NotNull String fallback);

}

package org.maxgamer.quickshop.File;

import org.jetbrains.annotations.NotNull;

public interface IFile {

    @NotNull
    String getMessage(@NotNull String path, @NotNull String fallback);

}

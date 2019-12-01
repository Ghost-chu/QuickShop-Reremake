package org.maxgamer.quickshop.File;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public interface IFile {

    void create();

    @NotNull
    InputStream getInputStream();

    void reload();

    void save();

    @Nullable
    Object get(@NotNull String path);

    void set(@NotNull String path, @NotNull Object object);

}

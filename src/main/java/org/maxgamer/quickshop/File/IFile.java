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

    void set(@NotNull String path, @NotNull Object object);

    void setAndSave(@NotNull String path, @NotNull Object object);

    @Nullable
    <T> T get(@NotNull String path);

    @NotNull
    <T> T get(@NotNull String path, @NotNull T fallback);

    @NotNull
    <T> T getOrSet(@NotNull String path, @NotNull T fallback);

}

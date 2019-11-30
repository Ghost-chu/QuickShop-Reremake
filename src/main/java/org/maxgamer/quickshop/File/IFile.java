package org.maxgamer.quickshop.File;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public interface IFile {

    void create();

    @NotNull
    InputStream getInputStream();

    void reload();

    void save();

}

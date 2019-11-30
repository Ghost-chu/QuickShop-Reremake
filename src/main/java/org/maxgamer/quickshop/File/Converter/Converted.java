package org.maxgamer.quickshop.File.Converter;

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.File.IFile;

public interface Converted<X extends IFile, Y extends IFile> {

    @NotNull
    X convertX();

    @NotNull
    Y convertY();

}

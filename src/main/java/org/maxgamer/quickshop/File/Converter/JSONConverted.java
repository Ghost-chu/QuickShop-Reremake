package org.maxgamer.quickshop.File.Converter;

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.File.XMLFile;
import org.maxgamer.quickshop.File.YAMLFile;

public final class JSONConverted implements Converted<XMLFile, YAMLFile> {

    @NotNull
    @Override
    public XMLFile convertX() {
        return new XMLFile();
    }

    @NotNull
    @Override
    public YAMLFile convertY() {
        return new YAMLFile();
    }

}

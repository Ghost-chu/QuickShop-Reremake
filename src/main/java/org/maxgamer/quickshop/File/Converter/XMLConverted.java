package org.maxgamer.quickshop.File.Converter;

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.File.JSONFile;
import org.maxgamer.quickshop.File.YAMLFile;

public final class XMLConverted implements Converted<JSONFile, YAMLFile> {

    @NotNull
    @Override
    public JSONFile convertX() {
        return new JSONFile();
    }

    @NotNull
    @Override
    public YAMLFile convertY() {
        return new YAMLFile();
    }

}

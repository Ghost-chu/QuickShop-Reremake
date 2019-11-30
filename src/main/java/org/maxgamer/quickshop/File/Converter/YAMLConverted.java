package org.maxgamer.quickshop.File.Converter;

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.File.JSONFile;
import org.maxgamer.quickshop.File.XMLFile;

public final class YAMLConverted implements Converted<JSONFile, XMLFile> {

    @NotNull
    @Override
    public JSONFile convertX() {
        return new JSONFile();
    }

    @NotNull
    @Override
    public XMLFile convertY() {
        return new XMLFile();
    }

}

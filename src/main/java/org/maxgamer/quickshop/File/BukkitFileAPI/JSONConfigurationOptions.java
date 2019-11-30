package org.maxgamer.quickshop.File.BukkitFileAPI;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JSONConfigurationOptions extends FileConfigurationOptions {

    private int indent = 2;

    protected JSONConfigurationOptions(@NotNull JSONConfiguration configuration) {
        super(configuration);
    }

    @NotNull
    @Override
    public JSONConfiguration configuration() {
        return (JSONConfiguration) super.configuration();
    }

    @NotNull
    @Override
    public JSONConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @NotNull
    @Override
    public JSONConfigurationOptions pathSeparator(char value) {
        super.pathSeparator(value);
        return this;
    }

    @NotNull
    @Override
    public JSONConfigurationOptions header(@Nullable String value) {
        super.header(value);
        return this;
    }

    @NotNull
    @Override
    public JSONConfigurationOptions copyHeader(boolean value) {
        super.copyHeader(value);
        return this;
    }

    public int indent() {
        return indent;
    }

    @NotNull
    public JSONConfigurationOptions indent(int indent) {
        Validate.isTrue(indent >= 2, "Indent must be at least 2 characters");
        Validate.isTrue(indent <= 9, "Indent cannot be greater than 9 characters");

        this.indent = indent;
        return this;
    }
}

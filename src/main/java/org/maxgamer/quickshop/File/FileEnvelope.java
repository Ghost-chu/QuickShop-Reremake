package org.maxgamer.quickshop.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Mock.MckFileConfiguration;
import org.maxgamer.quickshop.Util.Copied;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

public abstract class FileEnvelope implements IFile {

    @NotNull
    private final Plugin plugin;

    @NotNull
    protected final File file;

    @NotNull
    private final Copied copied;

    @NotNull
    private final String resourcePath;

    protected final boolean loadDefault;

    @NotNull
    protected FileConfiguration fileConfiguration = new MckFileConfiguration();

    public FileEnvelope(@NotNull Plugin plugin, @NotNull File file, @NotNull String resourcePath, boolean loadDefault) {
        this.plugin = plugin;
        this.file = file;
        this.copied = new Copied(file);
        this.resourcePath = resourcePath;
        this.loadDefault = loadDefault;
    }

    @Override
    public void create() {
        if (file.exists()) {
            reload();
            return;
        }

        try {
            final File parent = file.getParentFile();

            if (parent != null) {
                parent.mkdirs();
            }

            file.createNewFile();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        if (loadDefault) {
            copied.accept(getInputStream());
        }

        reload();
    }

    @NotNull
    @Override
    public InputStream getInputStream() {
        return Objects.requireNonNull(plugin.getResource(resourcePath));
    }

    @Override
    public void save() {
        try {
            if (fileConfiguration instanceof MckFileConfiguration) {
                reload();
            }

            fileConfiguration.save(file);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Nullable
    @Override
    public Object get(@NotNull String path) {
        return fileConfiguration.get(path);
    }

    @Override
    public void set(@NotNull String path, @NotNull Object object) {
        fileConfiguration.set(path, object);
    }

}

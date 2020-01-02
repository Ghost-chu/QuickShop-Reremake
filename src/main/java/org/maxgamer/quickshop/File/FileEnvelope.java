package org.maxgamer.quickshop.File;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Mock.MckFileConfiguration;
import org.maxgamer.quickshop.Util.Copied;
import org.maxgamer.quickshop.Util.Location.LocationOf;
import org.maxgamer.quickshop.Util.Location.StringOf;
import org.maxgamer.quickshop.Util.Util;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
            throw new IllegalStateException(exception);
        }
    }

    @NotNull
    @Override
    public String saveToString() {
        return fileConfiguration.saveToString();
    }

    @Override
    public void loadFromString(@NotNull String data) throws InvalidConfigurationException {
        fileConfiguration.loadFromString(data);
    }

    @Nullable
    @Override
    public Object get(@NotNull String path) {
        return fileConfiguration.get(path);
    }

    @Nullable
    @Override
    public Object get(@NotNull String path, @Nullable Object fallback) {
        final Object object = fileConfiguration.get(path, fallback);

        if (object == null) {
            return fallback;
        }

        return object;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrSet(@NotNull String path, @NotNull T fallback) {
        final T object = (T) fileConfiguration.get(path, null);

        if (object != null) {
            return object;
        }

        set(path, fallback);

        return fallback;
    }

    @NotNull
    @Override
    public Location getLocation(@NotNull String path) {
        return new LocationOf(
            getString(path).orElse("")
        ).value();
    }

    @Override
    public void setLocation(@NotNull String path, @NotNull Location location) {
        set(
            path,
            new StringOf(location).asString()
        );
    }

    @NotNull
    @Override
    public ItemStack getCustomItemStack(@NotNull String path) {
        try {
            return Util.deserialize(path);
        } catch (Exception exception) {
            return new ItemStack(Material.AIR);
        }
    }

    @Override
    public void setCustomItemStack(@NotNull String path, @NotNull ItemStack itemStack) {
        fileConfiguration.set(path, Util.serialize(itemStack));
    }

    @NotNull
    @Override
    public Optional<String> getString(@NotNull String path) {
        return Optional.ofNullable(fileConfiguration.getString(path));
    }

    @Override
    public void set(@NotNull String path, @Nullable Object object) {
        fileConfiguration.set(path, object);
        save();
    }

    @NotNull
    @Override
    public List<String> getStringList(@NotNull String path) {
        return fileConfiguration.getStringList(path);
    }

    @Override
    public int getInt(@NotNull String path) {
        return fileConfiguration.getInt(path);
    }

    @Override
    public double getDouble(@NotNull String path) {
        return fileConfiguration.getDouble(path);
    }

    @Override
    public long getLong(@NotNull String path) {
        return fileConfiguration.getLong(path);
    }

    @Override
    public byte getByte(@NotNull String path) {
        return (byte) fileConfiguration.getInt(path);
    }

    @Override
    public short getShort(@NotNull String path) {
        return (short) fileConfiguration.getInt(path);
    }

    @Override
    public boolean getBoolean(@NotNull String path) {
        return fileConfiguration.getBoolean(path);
    }

    @NotNull
    @Override
    public ConfigurationSection createSection(@NotNull String path) {
        final ConfigurationSection configurationSection = fileConfiguration.createSection(path);

        save();

        return configurationSection;
    }

    @NotNull
    @Override
    public ConfigurationSection getSection(@NotNull String path) {
        final ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection(path);

        return configurationSection == null ? new MckFileConfiguration() : configurationSection;
    }

    @NotNull
    @Override
    public ConfigurationSection getOrCreateSection(@NotNull String path) {
        ConfigurationSection section = getSection(path);

        if (section instanceof MckFileConfiguration) {
            section = createSection(path);
        }

        return section;
    }

}

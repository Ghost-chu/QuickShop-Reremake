package org.maxgamer.quickshop.File;

import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class FileEnvelope implements IFile {

    @NotNull
    protected final Plugin plugin;

    @NotNull
    protected final File file;

    @NotNull
    protected final String resourcePath;

    public FileEnvelope(@NotNull Plugin plugin, @NotNull File file, @NotNull String resourcePath) {
        this.plugin = plugin;
        this.file = file;
        this.resourcePath = resourcePath;
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
                file.getParentFile().mkdirs();
            }

            file.createNewFile();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        copy(getInputStream());
        reload();
    }

    @NotNull
    @Override
    public InputStream getInputStream() {
        final InputStream inputStream = plugin.getResource(resourcePath);

        if (inputStream == null) {
            throw new RuntimeException("The " + resourcePath + " file that expected  does not exist!");
        }

        return inputStream;
    }

    private void copy(@NotNull final InputStream inputStream) {
        try(final OutputStream out = new FileOutputStream(file)) {
            final byte[] buf = new byte[1024];
            int len;

            while((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            inputStream.close();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

}

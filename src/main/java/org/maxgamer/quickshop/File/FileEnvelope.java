package org.maxgamer.quickshop.File;

import org.bukkit.plugin.Plugin;
import org.cactoos.io.InputOf;
import org.cactoos.io.InputStreamOf;
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
                parent.mkdirs();
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
        return new InputStreamOf(
            new InputOf(
                plugin.getResource(resourcePath)
            )
        );
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

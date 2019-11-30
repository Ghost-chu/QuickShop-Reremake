package org.maxgamer.quickshop.File;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class FileEnvelope implements IFile {

    @NotNull
    private final Plugin plugin;

    @NotNull
    private final File file;

    @NotNull
    private final String resourcePath;

    @Override
    public void create() {
        if (file.exists()) {
            reload();
            return;
        }

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        final InputStream inputStream = plugin.getResource(resourcePath);

        if (inputStream == null) {
            throw new RuntimeException("The" + resourcePath + " file that expected  does not exist!");
        }

        copy(inputStream);
        reload();
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

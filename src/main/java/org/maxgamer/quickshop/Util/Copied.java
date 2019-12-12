package org.maxgamer.quickshop.Util;

import org.cactoos.Proc;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public final class Copied implements Proc<InputStream> {

    @NotNull
    private final File file;

    public Copied(@NotNull File file) {
        this.file = file;
    }

    @Override
    public void exec(@NotNull InputStream inputStream) {
        try(final OutputStream out = new FileOutputStream(file)) {
            final byte[] buf = new byte[1024];
            int len;

            while((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            inputStream.close();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

}

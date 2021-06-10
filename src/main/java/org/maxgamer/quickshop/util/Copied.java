/*
 * This file is a part of project QuickShop, the name is Copied.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * Utilities to copy data from InputStream to File
 *
 * @author portlek
 */
public class Copied implements Consumer<InputStream> {

    @NotNull
    private final File file;

    public Copied(@NotNull File file) {
        this.file = file;
    }

    @Override
    public void accept(@NotNull InputStream inputStream) {
        try (OutputStream out = new FileOutputStream(file);
             InputStream autoClosedInputStream = inputStream) {
            final byte[] buf = new byte[1024];
            int len;

            while ((len = autoClosedInputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

}

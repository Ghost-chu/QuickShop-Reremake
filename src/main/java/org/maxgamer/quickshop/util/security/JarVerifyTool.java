/*
 * This file is a part of project QuickShop, the name is JarVerifyTool.java
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

package org.maxgamer.quickshop.util.security;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarVerifyTool {
    @NotNull
    public List<JarEntry> verify(@NotNull JarFile jar) throws IOException {
        List<JarEntry> modified = new ArrayList<>();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            try {
                byte[] buffer = new byte[8192];
                InputStream is = jar.getInputStream(entry);
                //noinspection StatementWithEmptyBody
                while ((is.read(buffer, 0, buffer.length)) != -1) {
                    // We just read. This will throw a SecurityException
                    // if a signature/digest check fails.
                }
            } catch (SecurityException se) {
                modified.add(entry);
            }
        }
        return modified;
    }
}

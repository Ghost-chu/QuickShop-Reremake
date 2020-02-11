/*
 * MIT License
 *
 * Copyright © 2020 Bukkit Commons Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.maxgamer.quickshop.utils;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class DebugLogger extends TimerTask {
    private File logFile;
    private BufferedWriter bufferedWriter;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[HH:mm:ss] ");
    private LinkedList<String> caching = new LinkedList<>();

    @SneakyThrows
    public DebugLogger(@NotNull File logFile) {
        this.logFile = logFile;
        if (!this.logFile.exists()) {
            this.logFile.mkdirs();
            this.logFile.createNewFile();
        }
        OutputStream out;
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, false), StandardCharsets.UTF_8));
        new Timer().scheduleAtFixedRate(this, 0, 10000);
    }

    @SneakyThrows
    public void log(@NotNull String data) {
        caching.add(simpleDateFormat.format(new Date(System.currentTimeMillis())) + data);
    }


    @Override
    public void run() {
        //noinspection unchecked
        LinkedList<String> copy = (LinkedList<String>) caching.clone();
        caching = new LinkedList<>(); //Fast replace
        for (String line : copy) {
            try {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            } catch (IOException ignored) {
            }
        }
    }
}

package org.maxgamer.quickshop.util.updater;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface QuickUpdater {
    @NotNull VersionType getCurrentRunning();

    boolean isLatest(@NotNull VersionType versionType) throws IOException;

    @NotNull byte[] update(@NotNull VersionType versionType) throws IOException;

    void install(byte[] bytes) throws IOException;
}

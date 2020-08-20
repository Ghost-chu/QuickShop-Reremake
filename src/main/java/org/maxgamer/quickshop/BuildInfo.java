/*
 * This file is a part of project QuickShop, the name is BuildInfo.java
 *  Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Getter
public class BuildInfo {
    private final int buildId;
    private final String buildTag;
    private final String buildUrl;
    private final String gitCommit;
    private final String gitBranch;
    private final String pomGruopId;
    private final String pomArtifactId;

    public BuildInfo(@Nullable InputStream inputStream) {
        if (inputStream == null) {
            buildId = 0;
            buildTag = "Unknown";
            buildUrl = "Unknown";
            gitCommit = "Custom Build";
            gitBranch = "Unknown";
            pomGruopId = "Unknown";
            pomArtifactId = "Unknown";
            return;
        }
        YamlConfiguration buildInfo = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
        buildId = buildInfo.getInt("build-id", 0);
        buildTag = buildInfo.getString("build-tag", "Unknown");
        buildUrl = buildInfo.getString("build-url", "Unknown");
        gitCommit = buildInfo.getString("git-commit", "Invalid");
        gitBranch = buildInfo.getString("git-branch", "Unknown");
        pomGruopId = buildInfo.getString("pom-groupid", "Unknown");
        pomArtifactId = buildInfo.getString("pom-artifactid", "Unknown");
        try {
            inputStream.close();
        } catch (IOException ignored) {
        }
    }
}

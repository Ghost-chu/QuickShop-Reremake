package org.maxgamer.quickshop.Util;

import lombok.*;

@AllArgsConstructor
public class UpdateInfomation {
    private String version = null;
    private boolean isBeta = false;

    /**
     * Get newest version on SpigotMC.org
     *
     * @return The newest version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get target build is or not a beta build.
     *
     * @return Build status
     */
    public boolean getIsBeta() {
        return isBeta;
    }
}

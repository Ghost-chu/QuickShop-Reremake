package org.maxgamer.quickshop.Util;

public class UpdateInfomation {
    private String version = null;
    private boolean isNewUpdate = false;
    private boolean isBeta = false;

    UpdateInfomation(String version, boolean isNewUpdate, boolean isBeta) {
        this.version = version;
        this.isNewUpdate = isNewUpdate;
        this.isBeta = isBeta;
    }

    /**
     * Get newest version on SpigotMC.org
     * @return The newest version.
     */
    public String getVersion() {
        return version;
    }
    /**
     * Get target is or not a new update
     * @return Is have a new update
     */
    public boolean getIsNewUpdate() {
        return isNewUpdate;
    }
    /**
     * Get target build is or not a beta build.
     * @return Build status
     */
    public boolean getIsBeta() {
        return isBeta;
    }
}

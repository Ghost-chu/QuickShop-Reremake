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

    public String getVersion() {
        return version;
    }

    public boolean getIsNewUpdate() {
        return isNewUpdate;
    }

    public boolean getIsBeta() {
        return isBeta;
    }
}

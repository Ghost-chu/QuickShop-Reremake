package org.maxgamer.quickshop.NonQuickShopStuffs.me.lucko.spark.common.util;

public enum FormatUtil {
    ;

    public static String percent(double value, double max) {
        double percent = (value * 100d) / max;
        return (int) percent + "%";
    }

    public static String formatBytes(long bytes) {
        if (bytes == 0) {
            return "0 bytes";
        }
        String[] sizes = new String[]{"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int sizeIndex = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.1f", bytes / Math.pow(1024, sizeIndex)) + " " + sizes[sizeIndex];
    }
}

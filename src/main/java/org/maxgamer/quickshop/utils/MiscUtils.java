package org.maxgamer.quickshop.utils;

import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class MiscUtils {

    private MiscUtils() {
    }

    /**
     * Convert strArray to String. E.g "Foo, Bar"
     *
     * @param strArray Target array
     * @return str
     */
    @NotNull
    public static String array2String(final @NotNull String[] strArray) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strArray.length; i++) {
            builder.append(strArray[i]);
            if (i + 1 != strArray.length) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    /**
     * Convert strList to String. E.g "Foo, Bar"
     *
     * @param strList Target list
     * @return str
     */
    @NotNull
    public static String list2String(final @NotNull List<String> strList) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strList.size(); i++) {
            builder.append(strList.get(i));
            if (i + 1 != strList.size()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    /**
     * Match the map1 and map2
     *
     * @param map1 Map1
     * @param map2 Map2
     * @return Map1 match Map2
     */
    public static boolean mapMatches(final @NotNull Map<?, ?> map1, final @NotNull Map<?, ?> map2) {
        for (final Object obj : map1.keySet()) {
            if (!map2.containsKey(obj)) {
                return false;
            }
            if (map1.get(obj) != map2.get(obj)) {
                return false;
            }
        }
        return true;
    }
}

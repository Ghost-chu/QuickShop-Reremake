package org.maxgamer.quickshop.api;

import org.jetbrains.annotations.NotNull;

public class QuickShopAPI {
    protected static QuickShopAPI api;

    /**
     * Get api instance, you will get an object extends current class.
     * And you will need cast it to the version that you want to use.
     *
     * @param qapiVersion The version of api you want get.
     * @return
     */
    public static QuickShopAPI getApiInstance(QAPIVersion qapiVersion) {
        if (qapiVersion == null) {
            throw new IllegalArgumentException("QAPIVersion arg cannot be null");
        }
        if (api == null) {
            throw new IllegalStateException("QuickShop API not loaded yet, please try again later.");
        }
        switch (qapiVersion) {
            case V1:
                return api;
            default:
                throw new IllegalArgumentException("Cannot find the API (" + qapiVersion.name() + "), it not exist or removed.");
        }
    }

    /**
     * Set the api, you shouldn't use this method.
     *
     * @param clazz The clazz
     */
    @Deprecated
    public static void setupApiInstance(@NotNull QuickShopAPI clazz) {
        api = clazz;
    }

    public QAPIVersion getVersion() {
        return QAPIVersion.V1; //extend me
    }

}

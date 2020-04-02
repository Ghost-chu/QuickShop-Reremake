package org.maxgamer.quickshop.api.v1;

import org.maxgamer.quickshop.api.QAPIVersion;
import org.maxgamer.quickshop.api.QuickShopAPI;

public class QuickShopAPI_v1 extends QuickShopAPI {
    @Override
    public QAPIVersion getVersion() {
        return QAPIVersion.V1; //V1 impl
    }

}

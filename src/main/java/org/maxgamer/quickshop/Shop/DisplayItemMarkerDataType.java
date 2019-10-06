package org.maxgamer.quickshop.Shop;

import com.google.gson.Gson;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public class DisplayItemMarkerDataType implements PersistentDataType<byte[], DisplayItemMarker> {
    static final DisplayItemMarkerDataType INSTANCE = new DisplayItemMarkerDataType();
    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<DisplayItemMarker> getComplexType() {
        return DisplayItemMarker.class;
    }

    @Override
    public byte[] toPrimitive(DisplayItemMarker complex, PersistentDataAdapterContext context) {
        return new Gson().toJson(complex).getBytes();
    }

    @Override
    public DisplayItemMarker fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
        return new Gson().fromJson(new String(primitive),DisplayItemMarker.class);
    }
}

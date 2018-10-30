package org.maxgamer.quickshop.Shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.base.Optional;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.maxgamer.quickshop.QuickShop;

import java.util.UUID;

public class FakeItem {
    ItemStack iStack ;
    Location loc;
    int FakeEntityID;
    UUID RandomID;
    Vector vector;
    ProtocolManager protocolManager;
    private static int lastId = Integer.MAX_VALUE;
    public FakeItem(Location loc, final ItemStack item) {
        this.iStack = item;
        this.loc = loc.clone().add(0.5, 1, 0.5);
        this.FakeEntityID = getFakeEntityID();
        this.vector = new Vector(0, 0.1, 0);
        this.protocolManager = QuickShop.instance.getProtocolManager();
        RandomID = UUID.randomUUID();
        // @TODO
    }
    public int getFakeEntityID(){
        return lastId--;
    }
    public void create(){

        PacketContainer spawnEntity = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        StructureModifier<Object> mdf = spawnEntity.getModifier();
        mdf.write(0,FakeEntityID); //FakeUUID
        mdf.write(1,RandomID);
        mdf.write(3,loc.getX());
        mdf.write(4,loc.getY());
        mdf.write(5,loc.getZ());
        mdf.write(10,2);

        PacketContainer entityVelocity = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        StructureModifier<Integer> pint = entityVelocity.getIntegers();
        pint.write(1, 0);
        pint.write(2, 0);
        pint.write(3, 0);

        spawnEntity.getIntegers().write(0, FakeEntityID);
        final WrappedDataWatcher wr = new WrappedDataWatcher();
        final WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getItemStackSerializer(true);
        final WrappedDataWatcher.WrappedDataWatcherObject object = new WrappedDataWatcher.WrappedDataWatcherObject(6, serializer);
        wr.setObject(object, Optional.of(iStack));
        spawnEntity.getWatchableCollectionModifier().write(0, wr.getWatchableObjects());



    }
    public void remove() {

    }
}

/*
 * This file is a part of project QuickShop, the name is VirtualDisplayItem.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.maxgamer.quickshop.shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.event.ShopDisplayItemSpawnEvent;
import org.maxgamer.quickshop.util.AsyncPacketSender;
import org.maxgamer.quickshop.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualDisplayItem extends DisplayItem {


    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    //counter for ensuring ID is unique
    private static final AtomicInteger counter = new AtomicInteger(0);

    private static final String version = Util.getNMSVersion();

    //unique EntityID
    private final int entityID = counter.decrementAndGet();

    //The List which store packet sender
    private final Set<UUID> packetSenders = new ConcurrentSkipListSet<>();

    private volatile AsyncPacketSender.AsyncSendingTask asyncPacketSenderTask = null;

    private volatile boolean isDisplay;

    //If packet initialized
    private volatile boolean initialized = false;

    //packets
    private PacketContainer fakeItemPacket;

    private PacketContainer fakeItemMetaPacket;

    private PacketContainer fakeItemVelocityPacket;

    private PacketContainer fakeItemDestroyPacket;

    //packetListener
    private PacketAdapter packetAdapter;

    //cache chunk x and z
    private ShopChunk chunkLocation;



    public VirtualDisplayItem(@NotNull Shop shop) throws RuntimeException {
        super(shop);
    }

    private void initFakeDropItemPacket() {
        //First, create a new packet to spawn item
        fakeItemPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        //Location
        Location location = getDisplayLocation();

        //and add data based on packet class in NMS  (global scope variable)
        //Reference: https://wiki.vg/Protocol#Spawn_Object
        fakeItemPacket.getIntegers()
                //Entity ID
                .write(0, entityID)
                //Velocity x
                .write(1, 0)
                //Velocity y
                .write(2, 0)
                //Velocity z
                .write(3, 0)
                //Pitch
                .write(4, 0)
                //Yaw
                .write(5, 0);

        switch (version) {
            case "v1_13_R1":
            case "v1_13_R2":
                fakeItemPacket.getIntegers()
                        .write(6, 2)
                        //int data to mark
                        .write(7, 1);
                break;
            //int data to mark
            default:
                //for 1.14+, we should use EntityType
                fakeItemPacket.getEntityTypeModifier().write(0, EntityType.DROPPED_ITEM);
                //int data to mark
                fakeItemPacket.getIntegers().write(6, 1);
        }
//        if (version == 13) {
//            //for 1.13, we should use type id to represent the EntityType
//            //2->minecraft:item (Object ID:https://wiki.vg/Object_Data)
//            fakeItemPacket.getIntegers().write(6, 2);
//            //int data to mark
//            fakeItemPacket.getIntegers().write(7, 1);
//        } else {
//            //for 1.14+, we should use EntityType
//            fakeItemPacket.getEntityTypeModifier().write(0, EntityType.DROPPED_ITEM);
//            //int data to mark
//            fakeItemPacket.getIntegers().write(6, 1);
//        }
        //UUID
        fakeItemPacket.getUUIDs().write(0, UUID.randomUUID());
        //Location
        fakeItemPacket.getDoubles()
                //X
                .write(0, location.getX())
                //Y
                .write(1, location.getY())
                //Z
                .write(2, location.getZ());

        //Next, create a new packet to update item data (default is empty)
        fakeItemMetaPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        //Entity ID
        fakeItemMetaPacket.getIntegers().write(0, entityID);

        //List<DataWatcher$Item> Type are more complex
        //Create a DataWatcher
        WrappedDataWatcher wpw = new WrappedDataWatcher();
        //https://wiki.vg/index.php?title=Entity_metadata#Entity
        if (plugin.getConfig().getBoolean("shop.display-item-use-name")) {
            wpw.setObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromText(Util.getItemStackName(originalItemStack)).getHandle()));
            wpw.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        }

        //Must in the certain slot:https://wiki.vg/Entity_metadata#Item
        //For 1.13 is 6, and 1.14+ is 7
        switch (version) {
            case "v1_13_R1":
            case "v1_13_R2":
                wpw.setObject(6, WrappedDataWatcher.Registry.getItemStackSerializer(false), originalItemStack);
                break;
            default:
                wpw.setObject(7, WrappedDataWatcher.Registry.getItemStackSerializer(false), originalItemStack);
                break;
        }
//        wpw.setObject((version == 13 ? 6 : 7), WrappedDataWatcher.Registry.getItemStackSerializer(false), shop.getItem());
        //Add it
        fakeItemMetaPacket.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        //And, create a entity velocity packet to make it at a proper location (otherwise it will fly randomly)
        fakeItemVelocityPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        fakeItemVelocityPacket.getIntegers()
                //Entity ID
                .write(0, entityID)
                //Velocity x
                .write(1, 0)
                //Velocity y
                .write(2, 0)
                //Velocity z
                .write(3, 0);

        //Also make a DestroyPacket to remove it
        fakeItemDestroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        //Entity to remove
        fakeItemDestroyPacket.getIntegerArrays().write(0, new int[]{entityID});
        initialized = true;
    }

    @Override
    public boolean checkDisplayIsMoved() {
        return false;
    }

    @Override
    public boolean checkDisplayNeedRegen() {
        return false;
    }

    @Override
    public boolean checkIsShopEntity(@NotNull Entity entity) {
        return false;
    }

    @Override
    public void fixDisplayMoved() {

    }

    @Override
    public void fixDisplayNeedRegen() {

    }

    @Override
    public void remove() {
        sendPacketToAll(fakeItemDestroyPacket);
        unload();
        isDisplay = false;
    }

    private void sendPacketToAll(@NotNull PacketContainer packet) {
        Iterator<UUID> iterator = packetSenders.iterator();
        while (iterator.hasNext()) {
            Player nextPlayer = plugin.getServer().getPlayer(iterator.next());
            if (nextPlayer == null) {
                iterator.remove();
            } else {
                sendPacket(nextPlayer, packet);
            }
        }
    }


    private void sendPacket(@NotNull Player player, @NotNull PacketContainer packet) {
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("An error occurred when sending a packet", e);
        }
    }

    @Override
    public boolean removeDupe() {
        return false;
    }

    @Override
    public void respawn() {
        Util.ensureThread(false);
        remove();
        spawn();
    }

    public void sendFakeItemToAll() {
        sendPacketToAll(fakeItemPacket);
        sendPacketToAll(fakeItemMetaPacket);
        sendPacketToAll(fakeItemVelocityPacket);
    }

    @Override
    public void safeGuard(@Nullable Entity entity) {

    }

    @Override
    public void spawn() {
        Util.ensureThread(false);
        if (shop.isLeftShop()) {
            return;
        }
        //lazy initialize
        if (!initialized) {
            initFakeDropItemPacket();
        }
        if (shop.isDeleted() || !shop.isLoaded()) {
            return;
        }
        ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.VIRTUALITEM);
        plugin.getServer().getPluginManager().callEvent(shopDisplayItemSpawnEvent);
        if (shopDisplayItemSpawnEvent.isCancelled()) {
            Util.debugLog(
                    "Canceled the displayItem spawning because a plugin setCancelled the spawning event, usually this is a QuickShop Add on");
            return;
        }
        load();

        // Can't rely on the attachedShop cache to be accurate
        // So just try it and if it fails, no biggie
        /*try {
            shop.getAttachedShop().updateAttachedShop();
        } catch (NullPointerException ignored) {
        }*/

        sendFakeItemToAll();
        isDisplay = true;
    }

    //Due to the delay task in ChunkListener
    //We must move load task to first spawn to prevent some bug and make the check lesser
    private void load() {
        Util.ensureThread(false);
        //some time shop can be loaded when world isn't loaded
        if (Util.isLoaded(shop.getLocation())) {
            //Let nearby player can saw fake item
            Collection<Entity> entityCollection = shop.getLocation().getWorld().getNearbyEntities(shop.getLocation(), plugin.getServer().getViewDistance() * 16, shop.getLocation().getWorld().getMaxHeight(), plugin.getServer().getViewDistance() * 16);
            for (Entity entity : entityCollection) {
                if (entity instanceof Player) {
                    packetSenders.add(entity.getUniqueId()); //TODO Possibly Memory Leaking because this list never remove single object
                }
            }
        }

        if (asyncPacketSenderTask != null) {
            asyncPacketSenderTask.stop();
        }
        asyncPacketSenderTask = AsyncPacketSender.create();
        asyncPacketSenderTask.start(plugin);

        if (packetAdapter == null) {
            packetAdapter = new PacketAdapter(plugin, ListenerPriority.HIGH, PacketType.Play.Server.MAP_CHUNK) {  //TODO: Possibly memory leaking: The lambda point to memory leaking
                @Override
                public void onPacketSending(@NotNull PacketEvent event) {
                    //is really full chunk data
                    boolean isFull = event.getPacket().getBooleans().read(0);
                    if (!shop.isLoaded() || !isDisplay || !isFull || shop.isLeftShop()) {
                        return;
                    }
                    StructureModifier<Integer> integerStructureModifier = event.getPacket().getIntegers();
                    //chunk x
                    int x = integerStructureModifier.read(0);
                    //chunk z
                    int z = integerStructureModifier.read(1);
                    asyncPacketSenderTask.offer(() -> { //TODO: Possibly memory leaking: The lambda point to memory leaking
                        if (chunkLocation == null) {
                            World world = shop.getLocation().getWorld();
                            Chunk chunk;
                            try {
                                //sync getting chunk
                                chunk = plugin.getServer().getScheduler().callSyncMethod(plugin, () -> shop.getLocation().getChunk()).get();
                            } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException("An error occurred when getting chunk from the world", e);
                            }
                            chunkLocation = new ShopChunk(world.getName(), chunk.getX(), chunk.getZ());
                        }
                        Player player = event.getPlayer();
                        if (player instanceof TemporaryPlayer) {
                            return;
                        }
                        if (player == null || !player.isOnline()) {
                            return;
                        }
                        if (chunkLocation.isSame(player.getWorld().getName(), x, z)) {
                            packetSenders.add(player.getUniqueId());
                            sendFakeItem(player);
                        }
                    });
                }
            };
        }
        protocolManager.addPacketListener(packetAdapter); //TODO: This may affects performance
    }

    private void unload() {
        packetSenders.clear();
        if (packetAdapter != null) {
            protocolManager.removePacketListener(packetAdapter);
        }
        if (asyncPacketSenderTask != null) {
            asyncPacketSenderTask.stop();
        }
    }


    public void sendFakeItem(@NotNull Player player) {
        sendPacket(player, fakeItemPacket);
        sendPacket(player, fakeItemMetaPacket);
        sendPacket(player, fakeItemVelocityPacket);
    }

    @Override
    public @Nullable Entity getDisplay() {
        return null;
    }

    @Override
    public boolean isSpawned() {
        if (shop.isLeftShop()) {
            Shop aShop = shop.getAttachedShop();
            if (aShop instanceof ContainerShop) {
                return (Objects.requireNonNull(((ContainerShop) aShop).getDisplayItem())).isSpawned();
            }

        }
        return isDisplay;
    }

}

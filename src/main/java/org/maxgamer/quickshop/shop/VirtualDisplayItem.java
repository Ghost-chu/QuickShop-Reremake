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
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.AbstractDisplayItem;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.event.ShopDisplayItemSpawnEvent;
import org.maxgamer.quickshop.util.GameVersion;
import org.maxgamer.quickshop.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualDisplayItem extends AbstractDisplayItem {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private static final GameVersion VERSION = QuickShop.getInstance().getGameVersion();
    private static final ProtocolManager PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();
    private static PacketAdapter packetAdapter = null;
    //unique EntityID
    private final int entityID = COUNTER.decrementAndGet();
    //The List which store packet sender
    private final Set<UUID> packetSenders = new ConcurrentSkipListSet<>();
    //cache chunk x and z
    private JavaShopChunk chunkLocation;
    private volatile boolean isDisplay;
    //If packet initialized
    private volatile boolean initialized = false;
    //packets
    private PacketContainer fakeItemSpawnPacket;
    private PacketContainer fakeItemMetaPacket;
    private PacketContainer fakeItemVelocityPacket;
    private PacketContainer fakeItemDestroyPacket;

    public VirtualDisplayItem(@NotNull Shop shop) throws RuntimeException {
        super(shop);
        VirtualDisplayItemManager.load();
    }

    //Due to the delay task in ChunkListener
    //We must move load task to first spawn to prevent some bug and make the check lesser
    private void load() {
        Util.ensureThread(false);
        //some time shop can be loaded when world isn't loaded
        Chunk chunk = shop.getLocation().getChunk();
        chunkLocation = new JavaShopChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        VirtualDisplayItemManager.put(chunkLocation, this);
        if (Util.isLoaded(shop.getLocation())) {
            //Let nearby player can saw fake item
            Collection<Entity> entityCollection = shop.getLocation().getWorld().getNearbyEntities(shop.getLocation(), PLUGIN.getServer().getViewDistance() * 16, shop.getLocation().getWorld().getMaxHeight(), PLUGIN.getServer().getViewDistance() * 16);
            for (Entity entity : entityCollection) {
                if (entity instanceof Player) {
                    packetSenders.add(entity.getUniqueId());
                }
            }
        }
    }

    private void initFakeDropItemPacket() {
        fakeItemSpawnPacket = PacketFactory.createFakeItemSpawnPacket(entityID, getDisplayLocation());
        fakeItemMetaPacket = PacketFactory.createFakeItemMetaPacket(entityID, getOriginalItemStack().clone());
        fakeItemVelocityPacket = PacketFactory.createFakeItemVelocityPacket(entityID);
        fakeItemDestroyPacket = PacketFactory.createFakeItemDestroyPacket(entityID);
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
        if (isDisplay) {
            sendPacketToAll(fakeItemDestroyPacket);
            unload();
            isDisplay = false;
        }
    }

    private void sendPacketToAll(@NotNull PacketContainer packet) {
        Iterator<UUID> iterator = packetSenders.iterator();
        while (iterator.hasNext()) {
            Player nextPlayer = PLUGIN.getServer().getPlayer(iterator.next());
            if (nextPlayer == null) {
                iterator.remove();
            } else {
                sendPacket(nextPlayer, packet);
            }
        }
    }

    private void sendPacket(@NotNull Player player, @NotNull PacketContainer packet) {
        try {
            PROTOCOL_MANAGER.sendServerPacket(player, packet);
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
        sendPacketToAll(fakeItemSpawnPacket);
        sendPacketToAll(fakeItemMetaPacket);
        sendPacketToAll(fakeItemVelocityPacket);
    }

    @Override
    public void safeGuard(@Nullable Entity entity) {

    }

    @Override
    public void spawn() {
        Util.ensureThread(false);
        if (shop.isLeftShop() || isDisplay || shop.isDeleted() || !shop.isLoaded()) {
            return;
        }
        ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.VIRTUALITEM);
        PLUGIN.getServer().getPluginManager().callEvent(shopDisplayItemSpawnEvent);
        if (shopDisplayItemSpawnEvent.isCancelled()) {
            Util.debugLog(
                    "Canceled the displayItem spawning because a plugin setCancelled the spawning event, usually this is a QuickShop Add on");
            return;
        }

        //lazy initialize
        if (!initialized) {
            initFakeDropItemPacket();
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

    private void unload() {
        packetSenders.clear();
        VirtualDisplayItemManager.remove(chunkLocation, this);
    }

    public void sendFakeItem(@NotNull Player player) {
        sendPacket(player, fakeItemSpawnPacket);
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

    public static class VirtualDisplayItemManager {
        private static final AtomicBoolean LOADED = new AtomicBoolean(false);
        private static final Map<JavaShopChunk, List<VirtualDisplayItem>> CHUNKS_MAPPING = new ConcurrentHashMap<>();

        public static void put(@NotNull JavaShopChunk key, @NotNull VirtualDisplayItem value) {
            //Thread-safe was ensured by ONLY USE Map method to do something
            List<VirtualDisplayItem> virtualDisplayItems = new ArrayList<>(Collections.singletonList(value));
            CHUNKS_MAPPING.merge(key, virtualDisplayItems, (mapOldVal, mapNewVal) -> {
                mapOldVal.addAll(mapNewVal);
                return mapOldVal;
            });
        }

        public static void remove(@NotNull JavaShopChunk key, @NotNull VirtualDisplayItem value) {
            CHUNKS_MAPPING.computeIfPresent(key, (mapOldKey, mapOldVal) -> {
                mapOldVal.remove(value);
                return mapOldVal;
            });
        }

        public static void load() {
            if (LOADED.get()) {
                return;
            }
            Util.debugLog("Loading VirtualDisplayItem chunks mapping manager...");
            if (packetAdapter == null) {
                packetAdapter = new PacketAdapter(PLUGIN, ListenerPriority.HIGH, PacketType.Play.Server.MAP_CHUNK) {
                    @Override
                    public void onPacketSending(@NotNull PacketEvent event) {
                        //is really full chunk data
                        //In 1.17, this value was removed, so read safely
                        Boolean boxedIsFull = event.getPacket().getBooleans().readSafely(0);
                        boolean isFull = boxedIsFull == null || boxedIsFull;
                        if (!isFull) {
                            return;
                        }
                        Player player = event.getPlayer();
                        if (player instanceof TemporaryPlayer) {
                            return;
                        }
                        if (player == null || !player.isOnline()) {
                            return;
                        }
                        StructureModifier<Integer> integerStructureModifier = event.getPacket().getIntegers();
                        //chunk x
                        int x = integerStructureModifier.read(0);
                        //chunk z
                        int z = integerStructureModifier.read(1);

                        CHUNKS_MAPPING.computeIfPresent(new JavaShopChunk(player.getWorld().getName(), x, z), (chunkLocation, targetList) -> {
                            for (VirtualDisplayItem target : targetList) {
                                if (!target.shop.isLoaded() || !target.isDisplay || target.shop.isLeftShop()) {
                                    continue;
                                }
                                target.packetSenders.add(player.getUniqueId());
                                target.sendFakeItem(player);
                            }
                            return targetList;
                        });
                    }
                };
                Util.debugLog("Registering the packet listener...");
                PROTOCOL_MANAGER.addPacketListener(packetAdapter);
                LOADED.set(true);
            }
        }

        public static void unload() {
            Util.debugLog("Unloading VirtualDisplayItem chunks mapping manager...");
            if (LOADED.get()) {
                Util.debugLog("Unregistering the packet listener...");
                PROTOCOL_MANAGER.removePacketListener(packetAdapter);
                LOADED.set(false);
            }
        }
    }

    public static class PacketFactory {
        public static Throwable testFakeItem() {
            try {
                createFakeItemSpawnPacket(0, new Location(PLUGIN.getServer().getWorlds().get(0), 0, 0, 0));
                createFakeItemMetaPacket(0, new ItemStack(Material.values()[0]));
                createFakeItemVelocityPacket(0);
                createFakeItemDestroyPacket(0);
                return null;
            } catch (Throwable throwable) {
                return throwable;
            }
        }

        private static PacketContainer createFakeItemSpawnPacket(int entityID, Location displayLocation) {
            //First, create a new packet to spawn item
            PacketContainer fakeItemPacket = PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

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

            //noinspection SwitchStatementWithTooFewBranches
            switch (VERSION) {
//                case v1_13_R1:
//                case v1_13_R2:
//                    fakeItemPacket.getIntegers()
//                            //For 1.13, we should use type id to represent the EntityType
//                            //2 -> minecraft:item (Object ID:https://wiki.vg/Object_Data)
//                            .write(6, 2)
//                            //int data to mark
//                            .write(7, 1);
//                    break;
                //int data to mark
                default:
                    //For 1.14+, we should use EntityType
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
                    .write(0, displayLocation.getX())
                    //Y
                    .write(1, displayLocation.getY())
                    //Z
                    .write(2, displayLocation.getZ());
            return fakeItemPacket;
        }

        private static PacketContainer createFakeItemMetaPacket(int entityID, ItemStack itemStack) {
            //Next, create a new packet to update item data (default is empty)
            PacketContainer fakeItemMetaPacket = PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            //Entity ID
            fakeItemMetaPacket.getIntegers().write(0, entityID);

            //List<DataWatcher$Item> Type are more complex
            //Create a DataWatcher
            WrappedDataWatcher wpw = new WrappedDataWatcher();
            //https://wiki.vg/index.php?title=Entity_metadata#Entity
            if (PLUGIN.getConfig().getBoolean("shop.display-item-use-name")) {
                String itemName;
                if (QuickShop.isTesting()) {
                    //Env Testing
                    itemName = itemStack.getType().name();
                } else {
                    itemName = Util.getItemStackName(itemStack);
                }
                wpw.setObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromText(itemName).getHandle()));
                wpw.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true);
            }

            //Must in the certain slot:https://wiki.vg/Entity_metadata#Item
            //Is 1.17-?
            if (GameVersion.v1_17_R1.ordinal() > VERSION.ordinal()) {
//                if (version == GameVersion.v1_13_R1 || version == GameVersion.v1_13_R2) {
//                    //For 1.13 is 6
//                    wpw.setObject(6, WrappedDataWatcher.Registry.getItemStackSerializer(false), itemStack);
//                } else {
                //1.14-1.16 is 7
                wpw.setObject(7, WrappedDataWatcher.Registry.getItemStackSerializer(false), itemStack);
                // }
            } else {
                //1.17+ is 8
                wpw.setObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false), itemStack);
            }
            //Add it
            fakeItemMetaPacket.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
            return fakeItemMetaPacket;
        }

        private static PacketContainer createFakeItemVelocityPacket(int entityID) {
            //And, create a entity velocity packet to make it at a proper location (otherwise it will fly randomly)
            PacketContainer fakeItemVelocityPacket = PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
            fakeItemVelocityPacket.getIntegers()
                    //Entity ID
                    .write(0, entityID)
                    //Velocity x
                    .write(1, 0)
                    //Velocity y
                    .write(2, 0)
                    //Velocity z
                    .write(3, 0);
            return fakeItemVelocityPacket;
        }

        private static PacketContainer createFakeItemDestroyPacket(int entityID) {
            //Also make a DestroyPacket to remove it
            PacketContainer fakeItemDestroyPacket = PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            if (GameVersion.v1_17_R1.ordinal() > VERSION.ordinal()) {
                //On 1.17-, we need to write an integer array
                //Entity to remove
                fakeItemDestroyPacket.getIntegerArrays().write(0, new int[]{entityID});
            } else {
                //1.17+
                MinecraftVersion minecraftVersion = PROTOCOL_MANAGER.getMinecraftVersion();
                if (minecraftVersion.getMajor() == 1 && minecraftVersion.getMinor() == 17 && minecraftVersion.getBuild() == 0) {
                    //On 1.17, just need to write a int
                    //Entity to remove
                    fakeItemDestroyPacket.getIntegers().write(0, entityID);
                } else {
                    //On 1.17.1 (may be 1.17.1+? it's enough, Mojang, stop the changes), we need add the int list
                    //Entity to remove
                    try {
                        fakeItemDestroyPacket.getIntLists().write(0, Collections.singletonList(entityID));
                    } catch (NoSuchMethodError e) {
                        throw new RuntimeException("Unable to initialize packet, ProtocolLib update needed", e);
                    }
                }
            }
            return fakeItemDestroyPacket;
        }
    }
}

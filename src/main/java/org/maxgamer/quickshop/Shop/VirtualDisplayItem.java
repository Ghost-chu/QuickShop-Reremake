/*
 * This file is a part of project QuickShop, the name is VirtualDisplayItem.java
 * Copyright (C) sandtechnology <https://github.com/sandtechnology>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.maxgamer.quickshop.Shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class VirtualDisplayItem extends DisplayItem {


    private static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private volatile boolean isDisplay;
    //counter for ensuring ID is unique
    private static final AtomicInteger counter = new AtomicInteger(0);
    //unique EntityID
    private int entityID = counter.decrementAndGet();
    //packetListener
    private PacketAdapter packetListener = new PacketAdapter(plugin, ListenerPriority.NORMAL,
            PacketType.Play.Server.MAP_CHUNK) {
        @Override
        public void onPacketSending(PacketEvent event) {
            //is really full chunk data
            boolean isFull = event.getPacket().getBooleans().read(0);

            //if shop has deleted, unregister myself to ensure will be collected by GC
            if (shop.isDeleted()) {
                packetSenders.clear();
                protocolManager.removePacketListener(packetListener);
                return;
            }
            if (!isDisplay || !isFull || !Util.isLoaded(shop.getLocation())) {
                return;
            }
            //chunk x
            int x = event.getPacket().getIntegers().read(0);
            //chunk z
            int z = event.getPacket().getIntegers().read(1);

            //send the packet later to prevent chunk loading deadlock
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (shop.isLoaded() && shop.getLocation().getChunk().getX() == x && shop.getLocation().getChunk().getZ() == z) {
                    packetSenders.add(event.getPlayer().getUniqueId());
                    sendFakeItem(event.getPlayer());
                }
            }, 1);
        }
    };
    //packets
    private PacketContainer fakeItemPacket;
    private PacketContainer fakeItemMetaPacket;
    private PacketContainer fakeItemVelocityPacket;
    private PacketContainer fakeItemDestroyPacket;
    //The List which store packet sender
    private HashSet<UUID> packetSenders = new HashSet<>();


    public VirtualDisplayItem(@NotNull Shop shop) {
        super(shop);
        initFakeDropItemPacket();
        //Let nearby player can saw fake item
        packetSenders.addAll(shop.getLocation().getNearbyEntities(32, 256, 32).stream().filter(entity -> entity.getType() == EntityType.PLAYER).map(Entity::getUniqueId).collect(Collectors.toSet()));

        protocolManager.addPacketListener(packetListener);
        spawn();
    }
    private void initFakeDropItemPacket() {

        //First, create a new packet to spawn item
        fakeItemPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        //Location
        Location location = getDisplayLocation();

        //and add data based on packet class in NMS  (global scope variable)
        //Reference: https://wiki.vg/Protocol#Spawn_Object
        fakeItemPacket.getModifier()
                //Entity ID
                .write(0, entityID)
                //UUID
                .write(1,UUID.randomUUID())
                //X
                .write(2, location.getX())
                //Y
                .write(3, location.getY())
                //Z
                .write(4, location.getZ())
                //Velocity x
                .write(5,0)
                //Velocity y
                .write(6,0)
                //Velocity z
                .write(7,0)
                //Pitch
                .write(8,0)
                //Yaw
                .write(9,0)
                //int data to mark
                .write(11, 1);
        //EntityType
        fakeItemPacket.getEntityTypeModifier().write(0,EntityType.DROPPED_ITEM);

        //Next, create a new packet to update item data (default is empty)
        fakeItemMetaPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        //Entity ID
        fakeItemMetaPacket.getIntegers().write(0, entityID);

        //List<DataWatcher$Item> Type are more complex
        //Create a DataWatcher
        WrappedDataWatcher wpw = new WrappedDataWatcher();
        //Must in the seventh slot:https://wiki.vg/Entity_metadata#Item
        wpw.setObject(7, WrappedDataWatcher.Registry.getItemStackSerializer(false), shop.getItem());
        //Add it
        fakeItemMetaPacket.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        //And, create a entity velocity packet to make it at a proper location (otherwise it will fly randomly)
        fakeItemVelocityPacket=protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        fakeItemVelocityPacket.getIntegers()
                //Entity ID
                .write(0,entityID)
                //Velocity x
                .write(1,0)
                //Velocity y
                .write(2,0)
                //Velocity z
                .write(3,0);

        //Also make a DestroyPacket to remove it
        fakeItemDestroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        //Entity to remove
        fakeItemDestroyPacket.getIntegerArrays().write(0, new int[]{entityID});
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
    public boolean checkIsShopEntity(Entity entity) {
        return false;
    }

    public void sendFakeItem(Player player) {
        sendPacket(player, fakeItemPacket);
        sendPacket(player, fakeItemVelocityPacket);
        sendPacket(player, fakeItemMetaPacket);
    }

    public void sendFakeItemtoAll() {
        sendPacketToAll(fakeItemPacket);
        sendPacketToAll(fakeItemVelocityPacket);
        sendPacketToAll(fakeItemMetaPacket);
    }

    private void sendPacket(Player player, PacketContainer packet) {
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("An error occurred when sending a packet", e);
        }
    }

    private void sendPacketToAll(PacketContainer packet) {
        packetSenders.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        packetSenders.forEach(uuid -> sendPacket(Bukkit.getPlayer(uuid), packet));
    }

    @Override
    public void fixDisplayMoved() {

    }

    @Override
    public void fixDisplayNeedRegen() {

    }

    @Override
    public void remove() {
        isDisplay = false;
        sendPacketToAll(fakeItemDestroyPacket);
    }

    @Override
    public boolean removeDupe() {
        return false;
    }

    @Override
    public void respawn() {
        sendPacketToAll(fakeItemDestroyPacket);
        spawn();
    }

    @Override
    public void safeGuard(Entity entity) {

    }

    @Override
    public void spawn() {
        isDisplay = true;
        sendFakeItemtoAll();
    }

    @Override
    public Entity getDisplay() {
        return null;
    }

    @Override
    public Location getDisplayLocation() {
        return shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }

    @Override
    public boolean isSpawned() {
        return isDisplay;
    }

}

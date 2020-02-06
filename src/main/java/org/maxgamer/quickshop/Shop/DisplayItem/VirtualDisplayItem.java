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
package org.maxgamer.quickshop.Shop.DisplayItem;

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
            if (shop.isDelete()) {
                packetSenders.clear();
                protocolManager.removePacketListener(packetListener);
                return;
            }
            if (!isDisplay && !isFull) {
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
    private PacketContainer fakeItemTeleportPacket;
    private PacketContainer fakeItemDestroyPacket;
    //The List which store packet sender
    private HashSet<UUID> packetSenders = new HashSet<>();


    public VirtualDisplayItem(@NotNull Shop shop) {
        super(shop);
        initFakeDropItemPacket();
        packetSenders.addAll(shop.getLocation().getNearbyEntities(32, 256, 32).stream().filter(entity -> entity.getType() == EntityType.PLAYER).map(Entity::getUniqueId).collect(Collectors.toSet()));
        protocolManager.addPacketListener(packetListener);
        spawn();
    }

    public void removeSenders(Player player) {
        packetSenders.remove(player.getUniqueId());
    }

    private void initFakeDropItemPacket() {

        //First, create a new packet to spawn item
        fakeItemPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        //Location
        Location location = getDisplayLocation();
        fakeItemPacket.getIntegers()
                //Entity ID
                .write(0, entityID)
                //Pitch
                .write(1, (int) (location.getPitch() * 256.0F / 360.0F))
                //Yaw
                .write(2, (int) (location.getYaw() * 256.0F / 360.0F))
                //data
                .write(3, 1)
                //v x
                .write(4, 0)
                //v y
                .write(5, 0)
                //v z
                .write(6, 0);

        //Object UUID
        fakeItemPacket.getUUIDs().write(0, UUID.randomUUID());

        fakeItemPacket.getDoubles()
                //X
                .write(0, location.getX())
                //Y
                .write(1, location.getY())
                //Z
                .write(2, location.getZ());
        //EntityType
        fakeItemPacket.getEntityTypeModifier().write(0, EntityType.DROPPED_ITEM);

        //Next, create a new packet to update item data
        fakeItemMetaPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        fakeItemMetaPacket.getIntegers().write(0, entityID);
        WrappedDataWatcher wpw = new WrappedDataWatcher();
        wpw.setObject(7, WrappedDataWatcher.Registry.getItemStackSerializer(false), shop.getItem());
        fakeItemMetaPacket.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        //And, teleport it to a proper location
        fakeItemTeleportPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        //Entity ID
        fakeItemTeleportPacket.getIntegers().write(0, entityID);
        //Target Location
        fakeItemTeleportPacket.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());
        fakeItemTeleportPacket.getBytes()
                .write(0, (byte) (int) (location.getYaw() * 256.0F / 360.0F))
                .write(1, (byte) (int) (location.getPitch() * 256.0F / 360.0F));
        //On Ground
        fakeItemTeleportPacket.getBooleans().write(0, true);

        //Also make a DestroyPacket
        fakeItemDestroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        fakeItemDestroyPacket.getIntegerArrays().write(0, new int[]{entityID});
    }

    @Override
    public boolean checkDisplayIsMoved() {
        return true;
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
        sendPacket(player, fakeItemMetaPacket);
        //send packet later to fix item location
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendPacket(player, fakeItemTeleportPacket), 50);
    }

    public void sendFakeItemtoAll() {
        sendPacketToAll(fakeItemPacket);
        sendPacketToAll(fakeItemMetaPacket);
        //send packet later to fix item location
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendPacketToAll(fakeItemTeleportPacket), 50);
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
        sendPacketToAll(fakeItemTeleportPacket);
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

    @Override
    public void pendingRemoval() {
        pendingRemoval = true;
    }

    @Override
    public boolean isPendingRemoval() {
        return pendingRemoval;
    }

}

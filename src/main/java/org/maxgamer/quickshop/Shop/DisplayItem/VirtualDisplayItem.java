package org.maxgamer.quickshop.Shop.DisplayItem;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Shop.Shop;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class VirtualDisplayItem extends DisplayItem {


    private static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private volatile boolean isDisplay;
    //counter for ensure unique
    private static final AtomicInteger counter = new AtomicInteger(0);
    //placeholder for NullCheck
    private Entity placeholder = new FakeItem();
    //unique EntityID
    private int entityID = counter.decrementAndGet();
    //packetListener
    private PacketAdapter packetListener = new PacketAdapter(plugin, ListenerPriority.NORMAL,
            PacketType.Play.Server.MAP_CHUNK) {
        @Override
        public void onPacketSending(PacketEvent event) {
            //is really full chunk data
            boolean isFull = event.getPacket().getBooleans().read(0);
            if (!isDisplay && !isFull) {
                return;
            }
            //chunk x
            int x = event.getPacket().getIntegers().read(0);
            //chunk z
            int z = event.getPacket().getIntegers().read(1);

            //send the packet later to prevent chunk loading deadlock
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (shop.getLocation().getChunk().getX() == x && shop.getLocation().getChunk().getZ() == z) {
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
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendPacket(player, fakeItemTeleportPacket), 25);
    }

    public void sendFakeItemtoAll() {
        sendPacketToAll(fakeItemPacket);
        sendPacketToAll(fakeItemMetaPacket);
        //send packet later to fix item location
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendPacketToAll(fakeItemTeleportPacket), 25);
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
        protocolManager.removePacketListener(packetListener);
        sendPacketToAll(fakeItemDestroyPacket);
    }

    @Override
    public boolean removeDupe() {
        return false;
    }

    @Override
    public void respawn() {
        remove();
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
        return placeholder;
    }

    @Override
    public Location getDisplayLocation() {
        return shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }

    @Override
    public boolean isSpawned() {
        return isDisplay;
    }

    //placeHolder for return
    class FakeItem implements Entity {

        @Override
        public @NotNull Location getLocation() {
            return getDisplayLocation();
        }

        @Override
        public @Nullable Location getLocation(@Nullable Location location) {
            return getDisplayLocation();
        }

        @Override
        public @NotNull Vector getVelocity() {
            return null;
        }

        @Override
        public void setVelocity(@NotNull Vector vector) {

        }

        @Override
        public double getHeight() {
            return 0;
        }

        @Override
        public double getWidth() {
            return 0;
        }

        @Override
        public @NotNull BoundingBox getBoundingBox() {
            return null;
        }

        @Override
        public boolean isOnGround() {
            return true;
        }

        @Override
        public @NotNull World getWorld() {
            return null;
        }

        @Override
        public void setRotation(float v, float v1) {

        }

        @Override
        public boolean teleport(@NotNull Location location) {
            return false;
        }

        @Override
        public boolean teleport(@NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause teleportCause) {
            return false;
        }

        @Override
        public boolean teleport(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean teleport(@NotNull Entity entity, PlayerTeleportEvent.@NotNull TeleportCause teleportCause) {
            return false;
        }

        @Override
        public @NotNull List<Entity> getNearbyEntities(double v, double v1, double v2) {
            return null;
        }

        @Override
        public int getEntityId() {
            return entityID;
        }

        @Override
        public int getFireTicks() {
            return 0;
        }

        @Override
        public void setFireTicks(int i) {

        }

        @Override
        public int getMaxFireTicks() {
            return 0;
        }

        @Override
        public void remove() {

        }

        @Override
        public boolean isDead() {
            return false;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public void sendMessage(@NotNull String s) {

        }

        @Override
        public void sendMessage(@NotNull String[] strings) {

        }

        @Override
        public @NotNull Server getServer() {
            return plugin.getServer();
        }

        @Override
        public @NotNull String getName() {
            return "";
        }

        @Override
        public boolean isPersistent() {
            return false;
        }

        @Override
        public void setPersistent(boolean b) {

        }

        @Override
        public @Nullable Entity getPassenger() {
            return null;
        }

        @Override
        public boolean setPassenger(@NotNull Entity entity) {
            return false;
        }

        @Override
        public @NotNull List<Entity> getPassengers() {
            return null;
        }

        @Override
        public boolean addPassenger(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean removePassenger(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean eject() {
            return false;
        }

        @Override
        public float getFallDistance() {
            return 0;
        }

        @Override
        public void setFallDistance(float v) {

        }

        @Override
        public @Nullable EntityDamageEvent getLastDamageCause() {
            return null;
        }

        @Override
        public void setLastDamageCause(@Nullable EntityDamageEvent entityDamageEvent) {

        }

        @Override
        public @NotNull UUID getUniqueId() {
            return UUID.randomUUID();
        }

        @Override
        public int getTicksLived() {
            return 0;
        }

        @Override
        public void setTicksLived(int i) {

        }

        @Override
        public void playEffect(@NotNull EntityEffect entityEffect) {

        }

        @Override
        public @NotNull EntityType getType() {
            return EntityType.DROPPED_ITEM;
        }

        @Override
        public boolean isInsideVehicle() {
            return false;
        }

        @Override
        public boolean leaveVehicle() {
            return false;
        }

        @Override
        public @Nullable Entity getVehicle() {
            return null;
        }

        @Override
        public boolean isCustomNameVisible() {
            return false;
        }

        @Override
        public void setCustomNameVisible(boolean b) {

        }

        @Override
        public boolean isGlowing() {
            return false;
        }

        @Override
        public void setGlowing(boolean b) {

        }

        @Override
        public boolean isInvulnerable() {
            return false;
        }

        @Override
        public void setInvulnerable(boolean b) {

        }

        @Override
        public boolean isSilent() {
            return false;
        }

        @Override
        public void setSilent(boolean b) {

        }

        @Override
        public boolean hasGravity() {
            return false;
        }

        @Override
        public void setGravity(boolean b) {

        }

        @Override
        public int getPortalCooldown() {
            return 0;
        }

        @Override
        public void setPortalCooldown(int i) {

        }

        @Override
        public @NotNull Set<String> getScoreboardTags() {
            return Collections.emptySet();
        }

        @Override
        public boolean addScoreboardTag(@NotNull String s) {
            return false;
        }

        @Override
        public boolean removeScoreboardTag(@NotNull String s) {
            return false;
        }

        @Override
        public @NotNull PistonMoveReaction getPistonMoveReaction() {
            return null;
        }

        @Override
        public @NotNull BlockFace getFacing() {
            return null;
        }

        @Override
        public @NotNull Pose getPose() {
            return null;
        }

        @Override
        public @NotNull Spigot spigot() {
            return null;
        }

        @Override
        public @Nullable Location getOrigin() {
            return null;
        }

        @Override
        public boolean fromMobSpawner() {
            return false;
        }

        @Override
        public @NotNull Chunk getChunk() {
            return null;
        }

        @Override
        public CreatureSpawnEvent.@NotNull SpawnReason getEntitySpawnReason() {
            return null;
        }

        @Override
        public @Nullable String getCustomName() {
            return null;
        }

        @Override
        public void setCustomName(@Nullable String s) {

        }

        @Override
        public void setMetadata(@NotNull String s, @NotNull MetadataValue metadataValue) {

        }

        @Override
        public @NotNull List<MetadataValue> getMetadata(@NotNull String s) {
            return null;
        }

        @Override
        public boolean hasMetadata(@NotNull String s) {
            return false;
        }

        @Override
        public void removeMetadata(@NotNull String s, @NotNull Plugin plugin) {

        }

        @Override
        public boolean isPermissionSet(@NotNull String s) {
            return false;
        }

        @Override
        public boolean isPermissionSet(@NotNull Permission permission) {
            return false;
        }

        @Override
        public boolean hasPermission(@NotNull String s) {
            return false;
        }

        @Override
        public boolean hasPermission(@NotNull Permission permission) {
            return false;
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b) {
            return null;
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
            return null;
        }

        @Override
        public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b, int i) {
            return null;
        }

        @Override
        public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int i) {
            return null;
        }

        @Override
        public void removeAttachment(@NotNull PermissionAttachment permissionAttachment) {

        }

        @Override
        public void recalculatePermissions() {

        }

        @Override
        public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
            return null;
        }

        @Override
        public boolean isOp() {
            return false;
        }

        @Override
        public void setOp(boolean b) {

        }

        @Override
        public @NotNull PersistentDataContainer getPersistentDataContainer() {
            return null;
        }
    }
}

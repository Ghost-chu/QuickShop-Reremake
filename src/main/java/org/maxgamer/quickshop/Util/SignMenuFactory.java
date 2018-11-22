package org.maxgamer.quickshop.Util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class SignMenuFactory {

    public static final int POSITION_HEIGHT = 255;
    private static final int ACTION_INDEX = 9;
    private static final int SIGN_LINES = 4;

    private static final String NBT_FORMAT = "{\"text\":\"%s\"}";
    private static final String NBT_BLOCK_ID = "minecraft:sign";

    private final Plugin plugin;

    private final Map<Player, BiConsumer<Player, String[]>> inputReceivers;
    private final Map<Player, BlockPosition> signLocations;

    public SignMenuFactory(Plugin plugin) {
        this.plugin = plugin;
        this.inputReceivers = new HashMap<>();
        this.signLocations = new HashMap<>();
        this.listen();
    }

    public void newMenu(Player player, List<String> text, BiConsumer<Player, String[]> input) {
        Location location = player.getLocation();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), POSITION_HEIGHT, location.getBlockZ());

        player.sendBlockChange(blockPosition.toLocation(location.getWorld()), Material.WALL_SIGN.createBlockData());

        PacketContainer openSign = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        PacketContainer signData = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TILE_ENTITY_DATA);

        openSign.getBlockPositionModifier().write(0, blockPosition);

        NbtCompound signNBT = (NbtCompound) signData.getNbtModifier().read(0);

        IntStream.range(0, SIGN_LINES).forEach(line -> signNBT.put("Text" + (line + 1), text.size() > line ? String.format(NBT_FORMAT, this.color(text.get(line))) : ""));

        signNBT.put("x", blockPosition.getX());
        signNBT.put("y", blockPosition.getY());
        signNBT.put("z", blockPosition.getZ());
        signNBT.put("id", NBT_BLOCK_ID);

        signData.getBlockPositionModifier().write(0, blockPosition);
        signData.getIntegers().write(0, ACTION_INDEX);
        signData.getNbtModifier().write(0, signNBT);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, signData);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, openSign);
        } catch (InvocationTargetException exception) {
            exception.printStackTrace();
        }
        this.inputReceivers.putIfAbsent(player, input);
        this.signLocations.put(player, blockPosition);
    }

    private void listen() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.plugin, PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                String[] input = packet.getStringArrays().read(0);

                BiConsumer<Player, String[]> response = inputReceivers.remove(player);
                BlockPosition blockPosition = signLocations.remove(player);

                if (response == null || blockPosition == null) {
                    return;
                }
                event.setCancelled(true);
                response.accept(player, input);
                player.sendBlockChange(blockPosition.toLocation(player.getWorld()), Material.AIR.createBlockData());
            }
        });
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
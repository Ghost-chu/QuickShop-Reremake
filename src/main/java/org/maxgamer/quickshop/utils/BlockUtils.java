package org.maxgamer.quickshop.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockUtils {
  /**
   * Fetches the block which the given sign is attached to
   *
   * @param b The block which is attached
   * @return The block the sign is attached to
   */
  @Nullable
  public static Block getAttached(final @NotNull Block b) {
    final BlockData blockData = b.getBlockData();
    if (blockData instanceof Directional) {
      return b.getRelative(((Directional) blockData).getFacing().getOppositeFace());
    } else {
      return null;
    }
  }

  /**
   * Check the target block is double check.
   * @param b The block you want to check
   * @return Return the DoubleChestInventory if it is double chest, false for not.
   */
  public static DoubleChestInventory isDoubleChestAndGetInventory(@NotNull Block b) {
    return isDoubleChestAndGetInventory(b.getState());
  }
  /**
   * Check the target BlockState is double check.
   * @param b The block you want to check
   * @return Return the DoubleChestInventory if it is double chest, null for not.
   */
  @Nullable
  public static DoubleChestInventory isDoubleChestAndGetInventory(@NotNull BlockState b) {
    if (!(b instanceof Chest)) {
      return null;
    }
    final Inventory inv = ((Chest) b).getInventory();
    if(inv instanceof DoubleChestInventory){
      return (DoubleChestInventory)inv;
    }
    return null;
  }
}

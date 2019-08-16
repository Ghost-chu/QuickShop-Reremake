package org.maxgamer.quickshop.Command.SubCommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Info;
import org.maxgamer.quickshop.Shop.ShopAction;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

public class SubCommand_SuperCreate implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        ArrayList<String> list = new ArrayList<>();
        list.add(MsgUtil.getMessage("tabcomplete.amount"));
        return list;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item.getType() != Material.AIR) {
                    BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);
                    while (bIt.hasNext()) {
                        Block b = bIt.next();
                        if (Util.canBeShop(b)) {
                            BlockFace blockFace;
                            try {
                                blockFace = p.getFacing();
                            } catch (Throwable throwable) {
                                blockFace = Util.getYawFace(p.getLocation().getYaw());
                            }

                            if (!plugin.getShopManager().canBuildShop(p, b,
                                    blockFace)) {
                                // As of the new checking system, most plugins will tell the
                                // player why they can't create a shop there.
                                // So telling them a message would cause spam etc.
                                Util.debugLog("Util report you can't build shop there.");
                                return;
                            }

                            if (Util.getSecondHalf(b) != null && !QuickShop.getPermissionManager().hasPermission(sender,"quickshop.create.double")) {
                                p.sendMessage(MsgUtil.getMessage("no-double-chests"));
                                return;
                            }
                            if (Util.isBlacklisted(item.getType())
                                    && !QuickShop.getPermissionManager().hasPermission(p,"quickshop.bypass." + item.getType().name())) {
                                p.sendMessage(MsgUtil.getMessage("blacklisted-item"));
                                return;
                            }

                            if (cmdArg.length < 1) {
                                // Send creation menu.
                                Info info = new Info(b.getLocation(), ShopAction.CREATE,
                                        p.getInventory().getItemInMainHand(),
                                        b.getRelative(p.getFacing().getOppositeFace()));
                                plugin.getShopManager().getActions().put(p.getUniqueId(), info);
                                p.sendMessage(
                                        MsgUtil.getMessage("how-much-to-trade-for", Util.getItemStackName(item)));
                            } else {
                                plugin.getShopManager().handleChat(p, cmdArg[0], true);
                            }
                            return;
                        }
                    }
                    sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
            } else {
                sender.sendMessage(MsgUtil.getMessage("no-anythings-in-your-hand"));
            }
        } else {
            sender.sendMessage("This command can't be run by console");
        }
    }

}

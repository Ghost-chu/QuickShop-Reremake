package org.maxgamer.quickshop.Command.SubCommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;

public class SubCommand_Remove implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may use that command.");
            return;
        }
        Player p = (Player) sender;
        BlockIterator bIt = new BlockIterator(p, 10);
        if (!bIt.hasNext()) {
            sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
            return;
        }
        while (bIt.hasNext()) {
            Block b = bIt.next();
            Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                if (shop.getModerator().isModerator(((Player) sender).getUniqueId()) || QuickShop.getPermissionManager().hasPermission(p,"quickshop.other.destroy")) {
                    shop.onUnload();
                    shop.delete();
                } else {
                    sender.sendMessage(ChatColor.RED + MsgUtil.getMessage("no-permission"));
                }
                return;
            }
        }
        sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
    }
}

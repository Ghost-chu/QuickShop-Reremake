package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandContainer;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_Help implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        sendHelp(sender, commandLabel);
    }

    private void sendHelp(@NotNull CommandSender s, @NotNull String commandLabel) {
        s.sendMessage(MsgUtil.getMessage("command.description.title",s));
        for (CommandContainer container : plugin.getCommandManager().getCmds()) {
            List<String> requirePermissions = container.getPermissions();
            if (requirePermissions != null) {
                if (!requirePermissions.isEmpty()) {
                    for (String requirePermission : requirePermissions) {
                        if (requirePermission != null && !requirePermission.isEmpty() && !QuickShop.getPermissionManager().hasPermission(s, requirePermission)) {
                            //noinspection UnnecessaryContinue
                            continue;
                        }
                    }
                }
            }
            if (!container.isHidden()) {
                s.sendMessage(ChatColor.GREEN + "/" + commandLabel + " " + container
                        .getPrefix() + ChatColor.YELLOW + " - "
                        + MsgUtil.getMessage("command.description." + container.getPrefix(),s));
            }
        }
    }
}

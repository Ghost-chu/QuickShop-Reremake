package org.maxgamer.quickshop.Command;

import lombok.Data;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.SubCommands.*;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommandManager implements TabCompleter, CommandExecutor {
    private List<CommandContainer> cmds = new ArrayList<>();
    private CommandContainer rootContainer = CommandContainer.builder().prefix(null).permission(null)
            .executor(new SubCommand_ROOT()).build();

    public CommandManager() {
        registerCmd(CommandContainer.builder().prefix("help").permission(null).executor(new SubCommand_Help()).build());
        registerCmd(CommandContainer.builder().prefix("unlimited").permission("quickshop.unlimited")
                .executor(new SubCommand_Unlimited()).build());
        registerCmd(CommandContainer.builder().prefix("silentunlimited").hidden(true).permission("quickshop.unlimited")
                .executor(new SubCommand_SilentUnlimited()).build());
        registerCmd(CommandContainer.builder().prefix("slientunlimited").hidden(true).permission("quickshop.unlimited")
                .executor(new SubCommand_SilentUnlimited()).build());
        registerCmd(CommandContainer.builder().prefix("setowner").permission("quickshop.setowner")
                .executor(new SubCommand_SetOwner()).build());
        registerCmd(CommandContainer.builder().prefix("owner").hidden(true).permission("quickshop.setowner")
                .executor(new SubCommand_SetOwner()).build());
        registerCmd(CommandContainer.builder().prefix("amount").permission(null).executor(new SubCommand_Amount()).build());
        registerCmd(CommandContainer.builder().prefix("buy").permission("quickshop.create.buy").executor(new SubCommand_Buy())
                .build());
        registerCmd(CommandContainer.builder().prefix("sell").permission("quickshop.create.sell").executor(new SubCommand_Sell())
                .build());
        registerCmd(CommandContainer.builder().prefix("silentbuy").hidden(true).permission("quickshop.create.buy")
                .executor(new SubCommand_SilentBuy()).build());
        registerCmd(CommandContainer.builder().prefix("silentsell").hidden(true).permission("quickshop.create.sell")
                .executor(new SubCommand_SilentSell()).build());
        registerCmd(CommandContainer.builder().prefix("price").permission("quickshop.create.changeprice")
                .executor(new SubCommand_Price()).build());
        registerCmd(CommandContainer.builder().prefix("remove").permission(null).executor(new SubCommand_Remove()).build());
        registerCmd(CommandContainer.builder().prefix("silentremove").hidden(true).permission(null)
                .executor(new SubCommand_SilentRemove()).build());
        registerCmd(CommandContainer.builder().prefix("empty").permission("quickshop.empty").executor(new SubCommand_Empty())
                .build());
        registerCmd(CommandContainer.builder().prefix("refill").permission("quickshop.refill").executor(new SubCommand_Refill())
                .build());
        registerCmd(CommandContainer.builder().prefix("silentempty").hidden(true).permission("quickshop.empty")
                .executor(new SubCommand_SilentEmpty()).build());
        registerCmd(CommandContainer.builder().prefix("silentpreview").hidden(true).permission("quickshop.preview")
                .executor(new SubCommand_SilentPreview()).build());
        registerCmd(CommandContainer.builder().prefix("clean").permission("quickshop.clean").executor(new SubCommand_Clean())
                .build());
        registerCmd(CommandContainer.builder().prefix("reload").permission("quickshop.reload").executor(new SubCommand_Reload())
                .build());
        registerCmd(CommandContainer.builder().prefix("about").permission(null).executor(new SubCommand_About()).build());
        registerCmd(CommandContainer.builder().prefix("debug").permission("quickshop.debug").executor(new SubCommand_Debug())
                .build());
        registerCmd(CommandContainer.builder().prefix("fetchmessage").permission("quickshop.fetchmessage")
                .executor(new SubCommand_FetchMessage()).build());
        registerCmd(CommandContainer.builder().prefix("info").permission("quickshop.info").executor(new SubCommand_Info())
                .build());
        registerCmd(CommandContainer.builder().prefix("paste").permission("quickshop.paste").executor(new SubCommand_Paste())
                .build());
        registerCmd(CommandContainer.builder().prefix("staff").permission("quickshop.staff").executor(new SubCommand_Staff())
                .build());
        registerCmd(CommandContainer.builder().prefix("create").permission("quickshop.create.cmd")
                .permission("quickshop.create.sell")
                .executor(new SubCommand_Create())
                .build());
        registerCmd(CommandContainer.builder().prefix("update").hidden(true).permission("quickshop.alert")
                .executor(new SubCommand_Update())
                .build());
        registerCmd(CommandContainer.builder().prefix("find").permission("quickshop.find")
                .executor(new SubCommand_Find())
                .build());
        registerCmd(CommandContainer.builder().prefix("supercreate").permission("quickshop.create.admin")
                .permission("quickshop.create.sell")
                .executor(new SubCommand_SuperCreate())
                .build());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (QuickShop.instance.getBootError() != null) {
            QuickShop.instance.getBootError().printErrors(sender);
            return true;
        }

        if (sender instanceof Player) {
            if (QuickShop.instance.getConfig().getBoolean("effect.sound.ontabcomplete")) {
                Player player = (Player) sender;
                ((Player) sender).playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 80.0F, 1.0F);
            }
        }

        String[] passthroughArgs;
        if (cmdArg.length != 0) {
            passthroughArgs = new String[cmdArg.length - 1];
            System.arraycopy(cmdArg, 1, passthroughArgs, 0, passthroughArgs.length);
        } else {
            passthroughArgs = new String[0];
            Util.debugLog("Print help cause no args (/qs)");
            rootContainer.getExecutor().onCommand(sender, commandLabel, passthroughArgs);
            return true;
        }
        // if (cmdArg.length == 0)
        //     return rootContainer.getExecutor().onCommand(sender, commandLabel, temp);
        for (CommandContainer container : cmds) {
            Util.debugLog("Checking prefix with container: " + container.getPrefix() + " - " + cmdArg[0]);
            if (!container.getPrefix().toLowerCase().equals(cmdArg[0])) {
                continue;
            }
            Util.debugLog("Checking permission with container: " + container.getPrefix() + " - " + cmdArg[0]);
            List<String> requirePermissions = container.getPermissions();
            if (container.getPermissions() != null) {
                for (String requirePermission : requirePermissions) {

                    if (requirePermission != null && !requirePermission.isEmpty() && ! QuickShop.getPermissionManager().hasPermission(sender,requirePermission)) {
                        Util.debugLog("Sender " + sender.getName() + " trying execute the command: " + commandLabel + " " + Util
                                .array2String(cmdArg) + ", but no permission " + requirePermission);
                        sender.sendMessage(MsgUtil.getMessage("no-permission"));
                        return true;
                    }
                }
            }
            Util.debugLog("Execute container: " + container.getPrefix() + " - " + cmdArg[0]);
            container.getExecutor().onCommand(sender, commandLabel, passthroughArgs);
            return true;
        }
        Util.debugLog("All checks failed, print helps");
        rootContainer.getExecutor().onCommand(sender, commandLabel, passthroughArgs);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        //No args, it shouldn't happend
        if (sender instanceof Player) {
            if (QuickShop.instance.getConfig().getBoolean("effect.sound.ontabcomplete")) {
                Player player = (Player) sender;
                ((Player) sender).playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.0F, 1.0F);
            }
        }
        if (cmdArg.length == 0 || cmdArg.length == 1) {
            //No args
            return getRootContainer().getExecutor().onTabComplete(sender, commandLabel, cmdArg);
        }
        //Main args/more args
        String[] passthroughArgs;
        passthroughArgs = new String[cmdArg.length - 1];
        System.arraycopy(cmdArg, 1, passthroughArgs, 0, passthroughArgs.length);
        for (CommandContainer container : cmds) {
            Util.debugLog("Checking prefix with container: " + container.getPrefix() + " - " + cmdArg[0]);
            if (!container.getPrefix().toLowerCase().startsWith(cmdArg[0])) {
                continue;
            }
            Util.debugLog("Checking permission with container: " + container.getPrefix() + " - " + cmdArg[0]);
            List<String> requirePermissions = container.getPermissions();
            if (container.getPermissions() != null) {
                for (String requirePermission : requirePermissions) {
                    if (requirePermission != null && !requirePermission.isEmpty() && !QuickShop.getPermissionManager().hasPermission(sender,requirePermission)) {
                        Util.debugLog("Sender " + sender
                                .getName() + " trying tab-complete the command: " + commandLabel + " " + Util
                                .array2String(cmdArg) + ", but no permission " + requirePermission);
                        return null;
                    }
                }
            }
            Util.debugLog("Execute container: " + container.getPrefix());
            return container.getExecutor().onTabComplete(sender, commandLabel, passthroughArgs);
        }

        return null;
    }

    private void registerCmd(CommandContainer container) {
        if (!cmds.contains(container)) {
            cmds.add(container);
        }
    }
}

package org.maxgamer.quickshop.Command;

import java.util.ArrayList;
import java.util.List;

import lombok.*;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.SubCommands.*;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;

@Data
public class CommandManager implements TabCompleter, CommandExecutor {
    private List<CommandContainer> cmds = new ArrayList<>();
    private CommandContainer rootContainer = CommandContainer.builder().prefix(null).permission(null)
            .executor(new SubCommand_ROOT()).build();

    public CommandManager() {
        cmds.clear();
        registerCmd(CommandContainer.builder().prefix("help").permission(null).executor(new SubCommand_Unlimited()).build());
        registerCmd(CommandContainer.builder().prefix("unlimited").permission("quickshop.unlimited")
                .executor(new SubCommand_Unlimited()).build());
        registerCmd(CommandContainer.builder().prefix("silentunlimited").hidden(true).permission("quickshop.unlimited")
                .executor(new SubCommand_SilentUnlimited()).build());
        registerCmd(CommandContainer.builder().prefix("slientunlimited").hidden(true).permission("quickshop.unlimited")
                .executor(new SubCommand_SilentUnlimited()).build());
        registerCmd(CommandContainer.builder().prefix("setowner").permission("quickshop.setowner")
                .executor(new SubCommand_SetOwner()).build());
        registerCmd(CommandContainer.builder().prefix("owner").permission("quickshop.setowner")
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
    }

    private void registerCmd(CommandContainer container) {
        if (!cmds.contains(container))
            cmds.add(container);
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
        if (cmdArg.length == 0)
            return rootContainer.getExecutor().onTabComplete(sender, commandLabel, new String[]{});
        if (cmdArg.length == 1)
            return rootContainer.getExecutor().onTabComplete(sender, commandLabel, cmdArg);
        ArrayList<String> candidate = new ArrayList<>();
        for (CommandContainer container : cmds) {
            if (container.getPrefix().startsWith(cmdArg[0].toLowerCase()) || container.getPrefix().equals(cmdArg[0]
                    .toLowerCase()))
                if (container.getPermission() == null || container.getPermission().isEmpty() || sender.hasPermission(container
                        .getPermission()))
                    if (!container.isHidden())
                        candidate.add(container.getPrefix());
        }
        return candidate;

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
        String[] temp;
        if (cmdArg.length != 0) {
            temp = new String[cmdArg.length - 1];
            System.arraycopy(cmdArg, 1, temp, 0, temp.length)；
        } else {
            temp = new String[0];
        }
        if (cmdArg.length == 0)
            return rootContainer.getExecutor().onCommand(sender, commandLabel, temp);
        for (CommandContainer container : cmds) {
            if (container.getPrefix().equals(cmdArg[0].toLowerCase()))
                if (container.getPermission() == null || container.getPermission().isEmpty() || sender.hasPermission(container
                        .getPermission()))
                    return container.getExecutor().onCommand(sender, commandLabel, temp);
                else
                    sender.sendMessage(MsgUtil.getMessage("no-permission"));
        }
        return rootContainer.getExecutor().onCommand(sender, commandLabel, temp);
    }
}

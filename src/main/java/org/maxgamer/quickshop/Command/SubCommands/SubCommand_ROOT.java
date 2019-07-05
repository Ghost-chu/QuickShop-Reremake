package org.maxgamer.quickshop.Command.SubCommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.CommandContainer;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;

public class SubCommand_ROOT implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] strings) {
        List<String> candidate = new ArrayList<>();
        for (CommandContainer container : plugin.getCommandManager().getCmds()) {
            if (container.getPrefix().startsWith(strings[0]) || container.getPrefix().equals(strings[0])) {
                if (container.getPermission() == null || container.getPermission().isEmpty() || sender.hasPermission(container
                        .getPermission()))
                    if (!container.isHidden())
                        candidate.add(container.getPrefix());
            }
        }
        return candidate;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        new SubCommand_Help().onCommand(sender, commandLabel, cmdArg);
    }
}


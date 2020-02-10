package org.maxgamer.quickshop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShopLoader;

@CommandAlias("quickshop|qs")
public final class QuickShopCommand extends BaseCommand {

    @NotNull
    private final QuickShopLoader api;

    public QuickShopCommand(@NotNull QuickShopLoader api) {
        this.api = api;
    }

    @Default
    @CommandPermission("quickshop.command.main")
    public void mainCommand(CommandSender sender) {

    }

    @Subcommand("reload")
    @CommandPermission("quickshop.command.reload")
    public void reloadCommand(CommandSender sender) {

    }

}

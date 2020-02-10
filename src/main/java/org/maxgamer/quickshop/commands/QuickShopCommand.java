package org.maxgamer.quickshop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import io.github.portlek.configs.util.ListToString;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShopLoader;

@CommandAlias("quickshop|qs")
public final class QuickShopCommand extends BaseCommand {

    @NotNull
    private final QuickShopLoader loader;

    public QuickShopCommand(@NotNull QuickShopLoader loader) {
        this.loader = loader;
    }

    @Default
    @CommandPermission("quickshop.command.main")
    public void mainCommand(CommandSender sender) {
        sender.sendMessage(
            (String) loader.languageFile.help_messages.buildMap(list ->
                new ListToString(list).value()
            )
        );
    }

    @Subcommand("help")
    @CommandPermission("quickshop.command.help")
    public void helpCommand(CommandSender sender) {
        mainCommand(sender);
    }

    @Subcommand("reload")
    @CommandPermission("quickshop.command.reload")
    public void reloadCommand(CommandSender sender) {
        final long ms = System.currentTimeMillis();

        loader.reloadPlugin(false);
        sender.sendMessage(
            loader.languageFile.general.reload_complete.build(
                "%ms%", () -> String.valueOf(System.currentTimeMillis() - ms)
            )
        );
    }

}

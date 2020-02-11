/*
 * MIT License
 *
 * Copyright © 2020 Bukkit Commons Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.maxgamer.quickshop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import io.github.portlek.configs.util.ListToString;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShopLoader;

@RequiredArgsConstructor
@CommandAlias("quickshop|qs|qshop|shop")
public final class QuickShopCommand extends BaseCommand {

    @NotNull
    private final QuickShopLoader loader;

    @Default
    @CommandPermission("quickshop.command.main")
    public void mainCommand(CommandSender sender) {
        sender.sendMessage((String)
            loader.getLanguageFile().help_messages.buildMap(list ->
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
            loader.getLanguageFile().general.reload_complete.build(
                "%ms%", () -> String.valueOf(System.currentTimeMillis() - ms)
            )
        );
    }

    @Subcommand("version")
    @CommandPermission("quickshop.command.version")
    public void versionCommand(CommandSender sender) {
        loader.checkForUpdate(sender);
    }

}

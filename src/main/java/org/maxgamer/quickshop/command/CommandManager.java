/*
 * This file is a part of project QuickShop, the name is CommandManager.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.command;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.subcommand.*;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

@Data
public class CommandManager implements TabCompleter, CommandExecutor {
  private final Set<CommandContainer> cmds =
      Sets.newCopyOnWriteArraySet(); // Because we open to allow register, so
                                     // this should be thread-safe
  private final QuickShop plugin;
  private final CommandContainer rootContainer;

  public CommandManager(QuickShop plugin) {
    this.plugin = plugin;
    this.rootContainer = CommandContainer.builder()
                             .prefix(null)
                             .permission(null)
                             .executor(new SubCommand_ROOT(plugin))
                             .build();
    registerCmd(CommandContainer.builder()
                    .prefix("help")
                    .permission(null)
                    .executor(new SubCommand_Help(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("unlimited")
                    .permission("quickshop.unlimited")
                    .executor(new SubCommand_Unlimited(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("silentunlimited")
                    .hidden(true)
                    .permission("quickshop.unlimited")
                    .executor(new SubCommand_SilentUnlimited(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("slientunlimited")
                    .hidden(true)
                    .permission("quickshop.unlimited")
                    .executor(new SubCommand_SilentUnlimited(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("transfer")
                    .permission("quickshop.transfer")
                    .executor(new SubCommand_Transfer(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("setowner")
                    .permission("quickshop.setowner")
                    .executor(new SubCommand_SetOwner(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("owner")
                    .hidden(true)
                    .permission("quickshop.setowner")
                    .executor(new SubCommand_SetOwner(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("amount")
                    .permission(null)
                    .executor(new SubCommand_Amount(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("buy")
                    .permission("quickshop.create.buy")
                    .executor(new SubCommand_Buy(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("sell")
                    .permission("quickshop.create.sell")
                    .executor(new SubCommand_Sell(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("silentbuy")
                    .hidden(true)
                    .permission("quickshop.create.buy")
                    .executor(new SubCommand_SilentBuy(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("silentsell")
                    .hidden(true)
                    .permission("quickshop.create.sell")
                    .executor(new SubCommand_SilentSell(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("price")
                    .permission("quickshop.create.changeprice")
                    .executor(new SubCommand_Price(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("remove")
                    .permission(null)
                    .executor(new SubCommand_Remove(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("silentremove")
                    .hidden(true)
                    .permission(null)
                    .executor(new SubCommand_SilentRemove(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("empty")
                    .permission("quickshop.empty")
                    .executor(new SubCommand_Empty(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("refill")
                    .permission("quickshop.refill")
                    .executor(new SubCommand_Refill(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("silentempty")
                    .hidden(true)
                    .permission("quickshop.empty")
                    .executor(new SubCommand_SilentEmpty(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("silentpreview")
                    .hidden(true)
                    .permission("quickshop.preview")
                    .executor(new SubCommand_SilentPreview(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("clean")
                    .permission("quickshop.clean")
                    .executor(new SubCommand_Clean(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("reload")
                    .permission("quickshop.reload")
                    .executor(new SubCommand_Reload(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("about")
                    .permission(null)
                    .executor(new SubCommand_About(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("debug")
                    .permission("quickshop.debug")
                    .executor(new SubCommand_Debug(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("fetchmessage")
                    .permission("quickshop.fetchmessage")
                    .executor(new SubCommand_FetchMessage(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("info")
                    .permission("quickshop.info")
                    .executor(new SubCommand_Info(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("paste")
                    .permission("quickshop.paste")
                    .executor(new SubCommand_Paste(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("staff")
                    .permission("quickshop.staff")
                    .executor(new SubCommand_Staff(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("create")
                    .permission("quickshop.create.cmd")
                    .permission("quickshop.create.sell")
                    .executor(new SubCommand_Create(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("update")
                    .hidden(true)
                    .permission("quickshop.alerts")
                    .executor(new SubCommand_Update(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("find")
                    .permission("quickshop.find")
                    .executor(new SubCommand_Find(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("supercreate")
                    .permission("quickshop.create.admin")
                    .permission("quickshop.create.sell")
                    .executor(new SubCommand_SuperCreate(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("cleanghost")
                    .permission("quickshop.cleanghost")
                    .hidden(true)
                    .executor(new SubCommand_CleanGhost(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("reset")
                    .hidden(true)
                    .permission("quickshop.reset")
                    .executor(new SubCommand_Reset(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("recovery")
                    .hidden(true)
                    .permission("quickshop.recovery")
                    .executor(new SubCommand_Recovery(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("export")
                    .hidden(true)
                    .permission("quickshop.export")
                    .executor(new SubCommand_Export(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("convert")
                    .hidden(true)
                    .permission("quickshop.convert")
                    .executor(new SubCommand_Convert(plugin))
                    .build());
    if (plugin.isAllowStack()) {
      registerCmd(CommandContainer.builder()
                      .prefix("size")
                      .permission("quickshop.create.stacks")
                      .permission("quickshop.create.changeamount")
                      .executor(new SubCommand_Size(plugin))
                      .build());
    }
    registerCmd(CommandContainer.builder()
                    .prefix("item")
                    .permission("quickshop.create.changeitem")
                    .executor(new SubCommand_Item(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("removeall")
                    .permission(null)
                    .executor(new SubCommand_RemoveAll(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("removeworld")
                    .permission("quickshop.removeworld")
                    .executor(new SubCommand_RemoveWorld(plugin))
                    .build());
    registerCmd(CommandContainer.builder()
                    .prefix("currency")
                    .permission("quickshop.currency")
                    .executor(new SubCommand_Currency(plugin))
                    .build());
    //        registerCmd(CommandContainer.builder()
    //                .prefix("backup")
    //                .permission("quickshop.backup")
    //                .executor(new SubCommand_Backup(plugin))
    //                .hidden(true)
    //                .build());
  }

  /**
   * This is a interface to allow addons to register the subcommand into
   * quickshop command manager.
   *
   * @param container The command container to register
   * @throws IllegalStateException Will throw the error if register conflict.
   */
  public void registerCmd(@NotNull CommandContainer container) {
    if (cmds.contains(container)) {
      Util.debugLog("Dupe subcommand registering: " + container);
      return;
    }
    cmds.removeIf(commandContainer
                  -> commandContainer.getPrefix().equalsIgnoreCase(
                      container.getPrefix()));
    cmds.add(container);
  }

  /**
   * This is a interface to allow addons to unregister the registered/butil-in
   * subcommand from command manager.
   *
   * @param container The command container to unregister
   */
  public void unregisterCmd(@NotNull CommandContainer container) {
    cmds.remove(container);
  }

  /**
   * Gets a list contains all registered commands
   *
   * @return All registered commands.
   */
  @NotNull
  public List<CommandContainer> getRegisteredCommands() {
    return new ArrayList<>(this.getCmds());
  }

  @Override
  public boolean
  onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String commandLabel, @NotNull String[] cmdArg) {
    if (plugin.getBootError() != null) {
      if (cmdArg.length != 1) {
        plugin.getBootError().printErrors(sender);
        return true;
      }
      if (!cmdArg[0].equalsIgnoreCase("paste")) {
        plugin.getBootError().printErrors(sender);
        return true;
      }
    }

    if (sender instanceof Player &&
        plugin.getConfig().getBoolean("effect.sound.ontabcomplete")) {
      Player player = (Player)sender;
      ((Player)sender)
          .playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                     80.0F, 1.0F);
    }

    String[] passthroughArgs;
    if (cmdArg.length > 0) {
      passthroughArgs = new String[cmdArg.length - 1];
      System.arraycopy(cmdArg, 1, passthroughArgs, 0, passthroughArgs.length);
    } else {
      passthroughArgs = new String[0];
      Util.debugLog("Print help cause no args (/qs)");
      rootContainer.getExecutor().onCommand(sender, commandLabel,
                                            passthroughArgs);
      return true;
    }
    // if (cmdArg.length == 0)
    //     return rootContainer.getExecutor().onCommand(sender, commandLabel,
    //     temp);
    for (CommandContainer container : cmds) {
      if (!container.getPrefix().equalsIgnoreCase(cmdArg[0])) {
        continue;
      }
      List<String> requirePermissions = container.getPermissions();
      if (container.getPermissions() != null) {
        for (String requirePermission : requirePermissions) {

          if (requirePermission != null && !requirePermission.isEmpty() &&
              !QuickShop.getPermissionManager().hasPermission(
                  sender, requirePermission)) {
            Util.debugLog("Sender " + sender.getName() +
                          " trying execute the command: " + commandLabel + " " +
                          Util.array2String(cmdArg) + ", but no permission " +
                          requirePermission);
            MsgUtil.sendMessage(sender,
                                MsgUtil.getMessage("no-permission", sender));
            return true;
          }
        }
      }
      Util.debugLog("Execute container: " + container.getPrefix() + " - " +
                    cmdArg[0]);
      container.getExecutor().onCommand(sender, commandLabel, passthroughArgs);
      return true;
    }
    Util.debugLog("All checks failed, print helps");
    rootContainer.getExecutor().onCommand(sender, commandLabel,
                                          passthroughArgs);
    return true;
  }

  @Override
  public @Nullable List<String>
  onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                @NotNull String commandLabel, @NotNull String[] cmdArg) {
    // No args, it shouldn't happend
    if (plugin.getBootError() != null) {
      return null;
    }
    if (sender instanceof Player &&
        plugin.getConfig().getBoolean("effect.sound.ontabcomplete")) {
      Player player = (Player)sender;
      ((Player)sender)
          .playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.0F,
                     1.0F);
    }
    if (cmdArg.length == 0 || cmdArg.length == 1) {
      // No args
      return getRootContainer().getExecutor().onTabComplete(
          sender, commandLabel, cmdArg);
    }
    // Main args/more args
    String[] passthroughArgs;
    passthroughArgs = new String[cmdArg.length - 1];
    System.arraycopy(cmdArg, 1, passthroughArgs, 0, passthroughArgs.length);
    for (CommandContainer container : cmds) {
      if (!container.getPrefix().toLowerCase().startsWith(cmdArg[0])) {
        continue;
      }
      List<String> requirePermissions = container.getPermissions();
      if (container.getPermissions() != null) {
        for (String requirePermission : requirePermissions) {
          if (requirePermission != null && !requirePermission.isEmpty() &&
              !QuickShop.getPermissionManager().hasPermission(
                  sender, requirePermission)) {
            Util.debugLog("Sender " + sender.getName() +
                          " trying tab-complete the command: " + commandLabel +
                          " " + Util.array2String(cmdArg) +
                          ", but no permission " + requirePermission);
            return null;
          }
        }
      }
      Util.debugLog("Execute container: " + container.getPrefix());
      return container.getExecutor().onTabComplete(sender, commandLabel,
                                                   passthroughArgs);
    }

    return null;
  }
}

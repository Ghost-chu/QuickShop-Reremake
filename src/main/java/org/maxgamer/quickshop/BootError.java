package org.maxgamer.quickshop;

import lombok.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.*;

/**
 * BootError class contains print errors on /qs command when plugin failed launched.
 */
@EqualsAndHashCode
@ToString
public class BootError {

    private String[] errors;

    BootError(@NotNull String... errors) {
        this.errors = errors;
        for (String err : errors) {
            QuickShop.instance.getLogger().severe(err);
        }
    }

    /**
     * Print the errors.
     * #####################################################
     * QuickShop is disabled, Please fix errors and restart
     * ..........................
     * ####################################################
     * This one.
     *
     * @param sender The sender you want output the errors.
     */
    public void printErrors(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "#####################################################");
        sender.sendMessage(ChatColor.RED + " QuickShop is disabled, Please fix any errors and restart");
        for (String issue : errors) {
            sender.sendMessage(ChatColor.YELLOW + " " + issue);
        }
        //sender.sendMessage(ChatColor.YELLOW+" "+errors);
        sender.sendMessage(ChatColor.RED + "#####################################################");

    }

    public String[] getErrors() {
        return errors;
    }

}

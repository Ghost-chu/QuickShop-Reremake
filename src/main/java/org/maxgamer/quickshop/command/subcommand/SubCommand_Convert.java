package org.maxgamer.quickshop.command.subcommand;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.database.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@AllArgsConstructor
public class SubCommand_Convert implements CommandProcesser {
    private final QuickShop plugin;
    /**
     * Accept the onCommand, it will call when have Command Event cmdArg not contains
     * CommandContainer's prefix. E.g: Register the CommandContainer with Prefix: unlimited
     * Permission: quickshop.unlimited
     *
     * <p>When player type /qs unlimited 123 cmdArg's content is 123
     *
     * @param sender       Sender
     * @param commandLabel The command prefix /qs is qs
     * @param cmdArg       Args
     */
    @SneakyThrows
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "Danger command, please execute in console.");
            return;
        }
        if (cmdArg.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please select you want convert to: mysql or sqlite");
            return;
        }
        if (cmdArg[0].equalsIgnoreCase("mysql")) {
            if (plugin.getDatabase().getCore() instanceof MySQLCore) {
                sender.sendMessage(ChatColor.RED + "Please switch to SQLite before converting to MySQL.");
                return;
            }
            ConfigurationSection dbCfg = plugin.getConfig().getConfigurationSection("database");
            String user = dbCfg.getString("user");
            String pass = dbCfg.getString("password");
            String host = dbCfg.getString("host");
            String port = dbCfg.getString("port");
            String database = dbCfg.getString("database");
            boolean useSSL = dbCfg.getBoolean("usessl");
            DatabaseCore dbCore = new MySQLCore(plugin, Objects.requireNonNull(host, "MySQL host can't be null"), Objects.requireNonNull(user, "MySQL username can't be null"), Objects.requireNonNull(pass, "MySQL password can't be null"), Objects.requireNonNull(database, "MySQL database name can't be null"), Objects.requireNonNull(port, "MySQL port can't be null"), useSSL);
            Database databaseManager = new Database(dbCore);
            sender.sendMessage(ChatColor.GREEN + "Converting...");
            this.transferShops(new DatabaseHelper(plugin, databaseManager), sender);
            sender.sendMessage(ChatColor.GREEN + "All done, please edit config.yml to mysql to apply changes.");

        } else if (cmdArg[0].equalsIgnoreCase("sqlite")) {
            if (plugin.getDatabase().getCore() instanceof SQLiteCore) {
                sender.sendMessage(ChatColor.GREEN + "Please switch to MySQL before converting to SQLite.");
                return;
            }
            DatabaseCore core = new SQLiteCore(plugin, new File(plugin.getDataFolder(), "shops.db"));
            Database databaseManager = new Database(core);
            sender.sendMessage(ChatColor.GREEN + "Converting...");
            this.transferShops(new DatabaseHelper(plugin, databaseManager), sender);
            sender.sendMessage(ChatColor.GREEN + "All done, please edit config.yml to sqlite to apply changes.");

        } else {
            sender.sendMessage(ChatColor.RED + "Wrong type! Only can be mysql or sqlite");
        }
    }

    private void transferShops(@NotNull DatabaseHelper helper, @NotNull CommandSender sender) {
        plugin.getShopManager().getAllShops().forEach(shop -> {
            helper.removeShop(shop);
            helper.createShop(shop, null, (ignored) -> sender.sendMessage("Failed to convert shop " + shop));
        });
    }

    /**
     * Accept the onTabComplete, it will call when have Tab Event cmdArg not contains
     * CommandContainer's prefix. E.g: Register the CommandContainer with Prefix: unlimited
     * Permission: quickshop.unlimited
     *
     * <p>When player type /qs unlimited 123 cmdArg's content is 123
     *
     * @param sender       Sender
     * @param commandLabel The command prefix /qs is qs
     * @param cmdArg       Args
     * @return The result for tab-complete lists
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 2) {
            List<String> str = new ArrayList<>();
            str.add("sqlite");
            str.add("mysql");
            return str;
        }
        return null;
    }
}

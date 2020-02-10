package org.maxgamer.quickshop.file;

import io.github.portlek.configs.BukkitManaged;
import io.github.portlek.configs.annotations.Config;
import io.github.portlek.configs.annotations.Instance;
import io.github.portlek.configs.annotations.Section;
import io.github.portlek.configs.annotations.Value;
import io.github.portlek.configs.util.ColorUtil;
import io.github.portlek.configs.util.Replaceable;
import io.github.portlek.database.Database;
import io.github.portlek.database.SQL;
import io.github.portlek.database.database.MySQL;
import io.github.portlek.database.database.SQLite;
import io.github.portlek.database.sql.SQLBasic;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;

@Config(
    name = "config",
    location = "%basedir%/QuickShop"
)
public final class ConfigFile extends BukkitManaged {

    @NotNull
    public String plugin_language = "en";

    @Value
    public Replaceable<String> plugin_prefix = Replaceable.of("&6[&eQuickShop]&6")
        .map(ColorUtil::colored);

    @Instance
    public final Saving saving = new Saving();

    @Section(path = "saving")
    public class Saving {

        @Value
        public boolean save_when_plugin_disable = true;

        @Value
        public boolean auto_save = true;

        @Value
        public int auto_save_time = 60;

        @Value
        private String storage_type = "sqlite";

        @Instance
        public final MySQL mysql = new MySQL();

        @Section(path = "mysql")
        public class MySQL {

            @Value
            private String host = "localhost";

            @Value
            private int port = 3306;

            @Value
            private String database = "database";

            @Value
            private String username = "username";

            @Value
            private String password = "password";

        }

    }

    @NotNull
    public SQL createSQL() {
        final Database database;

        if (isMySQL()) {
            database = new MySQL(
                saving.mysql.host,
                saving.mysql.port,
                saving.mysql.database,
                saving.mysql.username,
                saving.mysql.password);
        } else {
            database = new SQLite(QuickShop.getInstance(), "shops.db");
        }

        return new SQLBasic(database);
    }

    private boolean isMySQL() {
        return saving.storage_type.equalsIgnoreCase("mysql") ||
            saving.storage_type.equalsIgnoreCase("remote") ||
            saving.storage_type.equalsIgnoreCase("net");
    }

}

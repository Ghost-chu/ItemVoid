package com.ghostchu.plugins.itemvoid.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.hikari.HikariConfig;
import cc.carm.lib.easysql.hikari.HikariDataSource;
import cc.carm.lib.easysql.manager.SQLManagerImpl;
import com.ghostchu.plugins.itemvoid.ItemVoid;
import org.bukkit.configuration.ConfigurationSection;
import org.h2.Driver;

import java.io.File;
import java.io.IOException;

public class DatabaseManager {
    private final ItemVoid plugin;
    private SQLManager sqlManager;
    private DatabaseDriverType databaseDriverType = null;
    private String prefix;
    private SimpleDatabaseHelper databaseHelper;

    public DatabaseManager(ItemVoid plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        ConfigurationSection databaseSection = plugin.getConfig().getConfigurationSection("database");
        if (databaseSection == null) throw new IllegalArgumentException("Database section cannot be null");
        HikariConfig config = HikariUtil.createHikariConfig(databaseSection.getConfigurationSection("properties"));
        try {
            this.prefix = databaseSection.getString("prefix");
            if (this.prefix == null || this.prefix.isBlank() || "none".equalsIgnoreCase(this.prefix)) {
                this.prefix = "";
            }
            if (databaseSection.getBoolean("mysql")) {
                this.sqlManager = connectMySQL(config, databaseSection);
            } else {
                this.sqlManager = connectH2(config,databaseSection);
            }
            databaseHelper = new SimpleDatabaseHelper(this.sqlManager, this.prefix);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to connect to database, please check database configuration", e);
        }
    }

    private SQLManager connectH2(HikariConfig config, ConfigurationSection dbCfg) throws IOException {
        databaseDriverType = DatabaseDriverType.H2;
        Driver.load();
        String driverClassName = Driver.class.getName();
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl("jdbc:h2:" + new File(plugin.getDataFolder(), "items").getCanonicalFile().getAbsolutePath() + ";MODE=MYSQL");
        SQLManager manager = new SQLManagerImpl(new HikariDataSource(config), "ItemVoid-SQLManager");
        manager.executeSQL("SET MODE=MYSQL"); // Switch to MySQL mode
        return manager;
    }

    private SQLManager connectMySQL(HikariConfig config, ConfigurationSection dbCfg) {
        databaseDriverType = DatabaseDriverType.MYSQL;
        // MySQL database - Required database be created first.
        String user = dbCfg.getString("user");
        String pass = dbCfg.getString("password");
        String host = dbCfg.getString("host");
        String port = dbCfg.getString("port");
        String database = dbCfg.getString("database");
        boolean useSSL = dbCfg.getBoolean("usessl");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL);
        config.setUsername(user);
        config.setPassword(pass);
        return new SQLManagerImpl(new HikariDataSource(config), "ItemVoid-SQLManager");
    }

    public DatabaseDriverType getDatabaseDriverType() {
        return databaseDriverType;
    }

    public SimpleDatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public String getPrefix() {
        return prefix;
    }
}

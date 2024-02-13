package com.ghostchu.plugins.itemvoid.database;

import cc.carm.lib.easysql.api.SQLManager;
import com.ghostchu.plugins.itemvoid.item.BakedVoidItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

public class SimpleDatabaseHelper {
    private final SQLManager sqlManager;
    private final String prefix;

    public SimpleDatabaseHelper(SQLManager sqlManager, String prefix) throws SQLException {
        this.sqlManager = sqlManager;
        this.prefix = prefix;
        checkTables();
    }

    private void checkTables() throws SQLException {
        DataTables.initializeTables(sqlManager, prefix);
    }

    public long[] saveItems(Collection<BakedVoidItem> items) {
        String SQL = "INSERT IGNORE INTO " + DataTables.ITEMS.getName() + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        //SQL = "INSERT INTO " + DataTables.ITEMS.getName() + " (discover_at, hash_sha256, material, name, lore, nbt, bukkit_yaml) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE hash_sha256=values(hash_sha256)";
        try (Connection connection = sqlManager.getConnection()) {
            for (BakedVoidItem voidItem : items) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {
                    preparedStatement.setLong(1, voidItem.getSha256());
                    preparedStatement.setTimestamp(2, new Timestamp(voidItem.getDiscoverAt()));
                    preparedStatement.setString(3, voidItem.getMaterial());
                    preparedStatement.setString(4, voidItem.getName());
                    preparedStatement.setString(5, voidItem.getLore());
                    preparedStatement.setString(6, voidItem.getNbt());
                    preparedStatement.setString(7, voidItem.getBukkitSerialized());
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new long[0];
    }

}

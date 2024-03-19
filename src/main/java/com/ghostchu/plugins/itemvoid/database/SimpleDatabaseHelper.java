package com.ghostchu.plugins.itemvoid.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.plugins.itemvoid.item.BakedVoidItem;
import com.ghostchu.plugins.itemvoid.item.DatabaseItem;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.configuration.InvalidConfigurationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SimpleDatabaseHelper {
    private final SQLManager sqlManager;
    private final String prefix;
    private final Cache<Long, Object> itemDuplicateCache = CacheBuilder.newBuilder()
            .initialCapacity(3000) // 由于缓存会以相当快的速度被填满，因此使初始容量匹配最大容量以避免扩展大小引发的性能开销
            .maximumSize(3000)
            .weakValues()
            .build();
    private final Object cachePlaceHolder = new Object();

    public SimpleDatabaseHelper(SQLManager sqlManager, String prefix) throws SQLException {
        this.sqlManager = sqlManager;
        this.prefix = prefix;
        checkTables();
    }

    private void checkTables() throws SQLException {
        DataTables.initializeTables(sqlManager, prefix);
    }

    public CompletableFuture<long[]> saveItems(Collection<BakedVoidItem> items) {
        return CompletableFuture.supplyAsync(() -> {
            Iterator<BakedVoidItem> it = items.iterator();
            while (it.hasNext()) {
                BakedVoidItem item = it.next();
                if (itemDuplicateCache.getIfPresent(item.getSha256()) != null) {
                    it.remove();
                } else {
                    itemDuplicateCache.put(item.getSha256(), cachePlaceHolder);
                }
            }
            if(items.isEmpty()) return new long[0];
            String SQL = "INSERT IGNORE INTO " + DataTables.ITEMS.getName() + " VALUES (?, ?, ?, ?, ?, ?, ?)";
            //SQL = "INSERT INTO " + DataTables.ITEMS.getName() + " (discover_at, hash_sha256, material, name, lore, nbt, bukkit_yaml) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE hash_sha256=values(hash_sha256)";
            try (Connection connection = sqlManager.getConnection()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {
                    for (BakedVoidItem voidItem : items) {
                        preparedStatement.setLong(1, voidItem.getSha256());
                        preparedStatement.setTimestamp(2, new Timestamp(voidItem.getDiscoverAt()));
                        preparedStatement.setString(3, voidItem.getMaterial());
                        preparedStatement.setString(4, voidItem.getName());
                        preparedStatement.setString(5, voidItem.getLore());
                        preparedStatement.setString(6, voidItem.getNbt());
                        preparedStatement.setString(7, voidItem.getBukkitSerialized());
                        preparedStatement.addBatch();
                    }
                    return preparedStatement.executeLargeBatch();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new long[0];
        });
    }

    public CompletableFuture<Collection<DatabaseItem>> queryByName(String keyword, int page, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            List<DatabaseItem> stackList = new ArrayList<>();
            try (SQLQuery query = DataTables.ITEMS.createQuery()
                    .addCondition("name", "LIKE", "%" + keyword + "%")
                    .orderBy("discover_at", false)
                    .setPageLimit((page - 1) * pageSize, pageSize)
                    .build().execute();
                 ResultSet set = query.getResultSet()) {
                while (set.next()) {
                    try {
                        long hashSha256 = set.getLong("hash_sha256");
                        Timestamp discoverAt = set.getTimestamp("discover_at");
                        String material = set.getString("material");
                        String name = set.getString("name");
                        String lore = set.getString("lore");
                        String nbt = set.getString("nbt");
                        String bukkitYaml = set.getString("bukkit_yaml");
                        stackList.add(new DatabaseItem(discoverAt.getTime(), hashSha256, name, lore, nbt, bukkitYaml, material));
                    } catch (InvalidConfigurationException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return stackList;
        });
    }

    public CompletableFuture<Collection<DatabaseItem>> queryByLore(String keyword, int page, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            List<DatabaseItem> stackList = new ArrayList<>();
            try (SQLQuery query = DataTables.ITEMS.createQuery()
                    .addCondition("lore", "LIKE", "%" + keyword + "%")
                    .orderBy("discover_at", false)
                    .setPageLimit((page - 1) * pageSize, pageSize)
                    .build().execute();
                 ResultSet set = query.getResultSet()) {
                while (set.next()) {
                    try {
                        long hashSha256 = set.getLong("hash_sha256");
                        Timestamp discoverAt = set.getTimestamp("discover_at");
                        String material = set.getString("material");
                        String name = set.getString("name");
                        String lore = set.getString("lore");
                        String nbt = set.getString("nbt");
                        String bukkitYaml = set.getString("bukkit_yaml");
                        stackList.add(new DatabaseItem(discoverAt.getTime(), hashSha256, name, lore, nbt, bukkitYaml, material));
                    } catch (InvalidConfigurationException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return stackList;
        });
    }


    public CompletableFuture<Collection<DatabaseItem>> queryByLoreFullText(String keyword, int page, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            List<DatabaseItem> stackList = new ArrayList<>();
            int startPos = (pageSize - 1) * pageSize;
            String SQL = "SELECT * FROM " + DataTables.ITEMS.getName() + " WHERE MATCH (`lore`) AGAINST (?) ORDER BY `discover_at` DESC LIMIT " + startPos + "," + pageSize;
            try (Connection connection = sqlManager.getConnection()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {
                    preparedStatement.setString(1, keyword);
                    try (ResultSet set = preparedStatement.executeQuery()) {
                        while (set.next()) {
                            try {
                                long hashSha256 = set.getLong("hash_sha256");
                                Timestamp discoverAt = set.getTimestamp("discover_at");
                                String material = set.getString("material");
                                String name = set.getString("name");
                                String lore = set.getString("lore");
                                String nbt = set.getString("nbt");
                                String bukkitYaml = set.getString("bukkit_yaml");
                                stackList.add(new DatabaseItem(discoverAt.getTime(), hashSha256, name, lore, nbt, bukkitYaml, material));
                            } catch (InvalidConfigurationException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return stackList;
        });
    }

}

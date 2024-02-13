package com.ghostchu.plugins.itemvoid;

import com.ghostchu.plugins.itemvoid.database.DatabaseManager;
import com.ghostchu.plugins.itemvoid.item.ItemVoidManager;
import com.ghostchu.plugins.itemvoid.listener.InventoryListener;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.Set;

public final class ItemVoid extends JavaPlugin {
    private static Set<InventoryType> COMPLEX_SEARCH_INVENTORY = ImmutableSet.of(InventoryType.CHEST);
    private DatabaseManager databaseManager;
    private ItemVoidManager itemVoidManager;
    private Random RANDOM = new Random();

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.databaseManager = new DatabaseManager(this);
        this.itemVoidManager = new ItemVoidManager();
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        Bukkit.getOnlinePlayers().forEach(this::collectFromEntity);
//        Bukkit.getWorlds().forEach(w -> {
//            for (Chunk loadedChunk : w.getLoadedChunks()) {
//                collectFromChunk(loadedChunk);
//            }
//        });
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            int count = itemVoidManager.getINSERT_QUEUE().size();
            if (count < 150) {
                if (RANDOM.nextBoolean()) {
                    return;
                }
            }
            itemVoidManager.pollItems(1200).thenAccept(bakedVoidItems -> databaseManager.getDatabaseHelper().saveItems(bakedVoidItems));
        }, 5 * new Random().nextInt(30), 20);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        itemVoidManager.pollItems(-1).thenAccept(bakedVoidItems -> databaseManager.getDatabaseHelper().saveItems(bakedVoidItems));
    }

    public ItemVoidManager getItemVoidManager() {
        return itemVoidManager;
    }


    public void collectFromChunk(Chunk chunk) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (BlockState tileEntity : chunk.getTileEntities()) {
                collectFromBlockState(tileEntity);
            }
            for (Entity entity : chunk.getEntities()) {
                collectFromEntity(entity);
            }
        }, 5L);
    }

    public void collectFromBlockState(BlockState state) {
        if (state instanceof InventoryHolder inventoryHolder) {
            itemVoidManager.discover(inventoryHolder.getInventory().getContents());
        }
    }

    public void collectFromEntity(Entity entity) {
        if (entity instanceof Player player) {
            itemVoidManager.discover(player.getInventory());
            itemVoidManager.discover(player.getEnderChest());
        } else if (entity instanceof ItemFrame itemFrame) {
            itemVoidManager.discover(itemFrame.getItem());
        } else if (entity instanceof InventoryHolder inventoryHolder) {
            itemVoidManager.discover(inventoryHolder.getInventory().getContents());
        }

    }

    public void collectFromInventory(Inventory inventory) {
        if (inventory.getType() == InventoryType.CREATIVE) {
            return;
        }
        ItemStack[] stacks = inventory.getContents();
        Block block = getInventoryBlockOrNull(inventory);
        if (block == null) {
            return;
        }
        itemVoidManager.discover(stacks);
    }

    private Block getInventoryBlockOrNull(Inventory inventory) {
        // Performs a fast Block object fetch for containers with only a single block, avoiding the need to fetch a
        // BlockState. When creating a BlockState Spigot will create a snapshot, which is a rather slow process.

        if (!COMPLEX_SEARCH_INVENTORY.contains(inventory.getType())) {
            Location inventoryLoc = inventory.getLocation();
            if (inventoryLoc != null) {
                return inventoryLoc.getBlock();
            }
            return null; // Custom GUI
        }

        // Complex search trick for Single Chest
        // If this is a single chest, we can do same fast fetch just like above
        if (inventory.getType() == InventoryType.CHEST) {
            Location inventoryLoc = inventory.getLocation();
            if (inventoryLoc != null) {
                Location invBlockLoc = inventoryLoc.clone();
                invBlockLoc.setX(inventoryLoc.getBlockX());
                invBlockLoc.setY(inventoryLoc.getBlockY());
                invBlockLoc.setZ(inventoryLoc.getBlockZ());
                // If the two Locations are the same, it means it is not a double chest;
                // The DoubleChest inventory location is similar like 94.5 64 88.5 (.5)
                if (inventoryLoc.equals(invBlockLoc)) {
                    return inventoryLoc.getBlock();
                }
            }
        }

        // We've exhausted our means, and now we'll have to use the slowest method just like before.
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof BlockState) {
            return ((BlockState) holder).getBlock();
        }
        if (holder instanceof DoubleChest) {
            InventoryHolder leftHolder = ((DoubleChest) holder).getLeftSide();
            if (leftHolder instanceof BlockState) {
                return ((BlockState) leftHolder).getBlock();
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            sender.sendMessage("Insert queue: " + itemVoidManager.getINSERT_QUEUE().size());
        });
        return true;
    }
}

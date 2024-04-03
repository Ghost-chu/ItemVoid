package com.ghostchu.plugins.itemvoid.listener;

import com.ghostchu.plugins.itemvoid.ItemVoid;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class InventoryListener implements Listener {
    public ItemVoid plugin;
    public InventoryListener(ItemVoid plugin){
        this.plugin = plugin;
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if(!plugin.getConfig().getBoolean("scan.inventory-open")){
           return;
        }
        plugin.collectFromInventory(event.getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(!plugin.getConfig().getBoolean("scan.player-join")){
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.collectFromInventory(event.getPlayer().getInventory()), 80L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(!plugin.getConfig().getBoolean("scan.player-quit")){
            return;
        }
        plugin.collectFromInventory(event.getPlayer().getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(!plugin.getConfig().getBoolean("scan.player-death")){
            return;
        }
        plugin.collectFromInventory(event.getEntity().getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPickup(EntityPickupItemEvent event) {
        if(!plugin.getConfig().getBoolean("scan.entity-pickup-item")){
            return;
        }
        plugin.getItemVoidManager().discover(event.getItem().getItemStack());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if(!plugin.getConfig().getBoolean("scan.chunk-load")){
            return;
        }
       plugin.collectFromChunk(event.getChunk());
    }
}

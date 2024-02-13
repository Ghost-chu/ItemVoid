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
        plugin.collectFromInventory(event.getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.collectFromInventory(event.getPlayer().getInventory()), 80L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.collectFromInventory(event.getPlayer().getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        plugin.collectFromInventory(event.getEntity().getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPickup(EntityPickupItemEvent event) {
        plugin.getItemVoidManager().discover(event.getItem().getItemStack());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
       plugin.collectFromChunk(event.getChunk());
    }
}

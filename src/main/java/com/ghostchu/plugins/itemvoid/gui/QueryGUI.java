package com.ghostchu.plugins.itemvoid.gui;

import com.ghostchu.plugins.itemvoid.ItemVoid;
import com.ghostchu.plugins.itemvoid.item.DatabaseItem;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class QueryGUI {
    private final ItemVoid plugin;
    private final Player player;
    private final ChestGui chestGui;
    private final OutlinePane body;
    private final StaticPane footer;
    private final String keyword;
    private final QueryMode queryMode;
    private int page = 1;
    private Collection<DatabaseItem> queryResult;

    public QueryGUI(ItemVoid plugin, Player player, String keyword, QueryMode queryMode) {
        this.plugin = plugin;
        this.player = player;
        this.keyword = keyword;
        this.queryMode = queryMode;
        this.chestGui = new ChestGui(6, "查询结果: " + keyword);
        this.body = new OutlinePane(0, 0, 9, 5);
        this.footer = new StaticPane(0, 5, 9, 1);
        chestGui.addPane(body);
        chestGui.addPane(footer);
    }

    public void open() {
        refreshGui();
        chestGui.show(player);
    }

    private void refreshGui() {
        enterQueryingMode();
        updateFooterPageIcons();
        chestGui.update();
        CompletableFuture.supplyAsync(() -> {
            reQuery();
            updateFooterPageIcons();
            updateBodyWithResult();
            return null;
        }).thenAccept((ignored) -> Bukkit.getScheduler().runTask(plugin, chestGui::update));
    }

    private void updateBodyWithResult() {
        body.clear();
        if (queryResult.isEmpty()) {
            for (int i = 0; i < body.getHeight() * body.getLength(); i++) {
                body.addItem(new GuiItem(noResultPlaceHolderItem(), cancelEvent()));
            }
            return;
        }
        for (DatabaseItem record : this.queryResult) {
            body.addItem(new GuiItem(record.getItemStack(), (event) -> {
                event.setResult(Event.Result.DENY);
                event.setCancelled(true);
                player.getInventory().addItem(record.getItemStack());
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1F, 1F);
                player.sendMessage(ChatColor.GREEN + "已添加到物品栏（如果没加上，请检查背包空间）");
            }));
        }
    }

    private void enterQueryingMode() {
        body.clear();
        for (int i = 0; i < body.getHeight() * body.getLength(); i++) {
            body.addItem(new GuiItem(queryingPlaceHolderItem(), cancelEvent()));
        }
    }

    private void updateFooterPageIcons() {
        footer.clear();
        ItemStack previousPage = new ItemStack(Material.PAPER);
        ItemStack currentPage = new ItemStack(Material.BOOK);
        ItemStack nextPage = new ItemStack(Material.PAPER);
        setItemStackName(previousPage, "<<上一页");
        setItemStackName(currentPage, "当前页：" + page);
        setItemStackName(nextPage, "下一页>>");
        currentPage.setAmount(Math.min(1, Math.max(page, currentPage.getMaxStackSize())));
        footer.addItem(new GuiItem(previousPage, e -> {
            e.setResult(Event.Result.DENY);
            e.setCancelled(true);
            page = Math.max(1, page - 1);
            refreshGui();
        }), 0, 0);
        footer.addItem(new GuiItem(currentPage, cancelEvent()), 4, 0);
        footer.addItem(new GuiItem(nextPage, e -> {
            e.setResult(Event.Result.DENY);
            e.setCancelled(true);
            page = page + 1;
            refreshGui();
        }), 8, 0);
    }


    private ItemStack noResultPlaceHolderItem() {
        ItemStack querying = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        setItemStackName(querying, ChatColor.GRAY + "无结果");
        return querying;
    }

    private Consumer<InventoryClickEvent> cancelEvent() {
        return e -> {
            e.setResult(Event.Result.DENY);
            e.setCancelled(true);
        };
    }

    private ItemStack queryingPlaceHolderItem() {
        ItemStack stack = new ItemStack(Material.BARRIER);
        setItemStackName(stack, ChatColor.YELLOW + "查询中，请等待...");
        return stack;
    }


    private void reQuery() {
        switch (queryMode) {
            case QUERY_NAME ->
                    this.queryResult = plugin.getDatabaseManager().getDatabaseHelper().queryByName(keyword, page, body.getLength() * body.getLength()).join();
            case QUERY_LORE ->
                    this.queryResult = plugin.getDatabaseManager().getDatabaseHelper().queryByLore(keyword, page, body.getLength() * body.getLength()).join();
            case QUERY_NAME_FULLTEXT ->
                    this.queryResult = plugin.getDatabaseManager().getDatabaseHelper().queryByNameFullText(keyword, page, body.getLength() * body.getLength()).join();
            case QUERY_LORE_FULLTEXT ->
                    this.queryResult = plugin.getDatabaseManager().getDatabaseHelper().queryByLoreFullText(keyword, page, body.getLength() * body.getLength()).join();
            case QUERY_EVERYTHING_FULLTEXT ->
                    this.queryResult = plugin.getDatabaseManager().getDatabaseHelper().queryByEverythingFullText(keyword, page, body.getLength() * body.getLength()).join();
        }
    }


    private void setItemStackName(ItemStack stack, String displayName) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + "" + ChatColor.WHITE + displayName);
        stack.setItemMeta(meta);
    }
}

package com.ghostchu.plugins.itemvoid.item;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class ItemVoidManager implements AutoCloseable {
    private Deque<VoidItem> INSERT_QUEUE = new ConcurrentLinkedDeque<>();
    private AtomicBoolean stopFilterThread = new AtomicBoolean(false);
    private static int MAX_PARSE_DEPTH = 3;

    public ItemVoidManager() {
    }

    public void discover(ItemStack stack) {
        if (stack == null) return;
        if (!stack.hasItemMeta()) return;
        INSERT_QUEUE.offer(new RawVoidItem(System.currentTimeMillis(), stack));
    }

    public void discover(ItemStack... stack) {
        for (ItemStack itemStack : stack) {
            discover(itemStack);
        }
    }

    public void discover(Iterable<ItemStack> stack) {
        for (ItemStack itemStack : stack) {
            discover(itemStack);
        }
    }

    public CompletableFuture<Collection<BakedVoidItem>> pollItems(int amount) {
        return CompletableFuture.supplyAsync(() -> {
            int n;
            if (amount < 0) {
                n = INSERT_QUEUE.size();
            } else {
                n = amount;
            }
            List<VoidItem> rawPool = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                VoidItem voidItem = INSERT_QUEUE.poll();
                if (voidItem == null) {
                    break;
                }
                rawPool.add(voidItem);
                rawPool.addAll(parsePossibleExtraContent(voidItem.getItemStack().getItemMeta(), 0).stream().map(item -> new RawVoidItem(voidItem.getDiscoverAt(), item)).toList());
            }
            return rawPool.parallelStream().filter(item -> isCollectItem(item.getItemStack())).map(BakedVoidItem::new).toList();
        });
    }

    private boolean isCollectItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (stack.getAmount() < 1) {
            return false;
        }
        if (stack.getType().isAir()) {
            return false;
        }
        if (!stack.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        return meta.hasCustomModelData() || meta.hasDisplayName() || meta.hasLore();
    }

    private Collection<ItemStack> parsePossibleExtraContent(ItemMeta meta, int depth) {
        if (depth > MAX_PARSE_DEPTH) {
            return Collections.emptyList();
        }
        try {
            Set<ItemStack> set = new HashSet<>();
            if (meta instanceof BlockStateMeta im) {
                if (im.getBlockState() instanceof InventoryHolder holder) {
                    for (ItemStack content : holder.getInventory().getContents()) {
                        if (content == null) continue;
                        if (content.hasItemMeta()) {
                            set.addAll(parsePossibleExtraContent(content.getItemMeta(), depth + 1));
                        } else {
                            set.add(content);
                        }
                    }
                }
            }
            return set;
        } catch (StackOverflowError error) {
            error.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public void close() throws Exception {
        stopFilterThread.set(true);
    }

    public Deque<VoidItem> getINSERT_QUEUE() {
        return INSERT_QUEUE;
    }
}

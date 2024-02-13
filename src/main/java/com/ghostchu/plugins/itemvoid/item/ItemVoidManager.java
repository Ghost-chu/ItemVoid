package com.ghostchu.plugins.itemvoid.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ItemVoidManager implements AutoCloseable {
    private Deque<VoidItem> INSERT_QUEUE = new ConcurrentLinkedDeque<>();
    private AtomicBoolean stopFilterThread = new AtomicBoolean(false);

    public ItemVoidManager() {
    }

    public void discover(ItemStack stack) {
        if (stack == null) return;
        if (!stack.hasItemMeta()) return;
        ItemMeta meta = stack.getItemMeta();
        if (meta.hasDisplayName() || meta.hasLore()) {
            INSERT_QUEUE.offer(new RawVoidItem(System.currentTimeMillis(), stack));
        }
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
            List<BakedVoidItem> pool = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                VoidItem voidItem = INSERT_QUEUE.pollFirst();
                if (voidItem == null) {
                    break;
                }
                pool.add(new BakedVoidItem(voidItem));
            }
            return pool;
        });
    }

    @Override
    public void close() throws Exception {
        stopFilterThread.set(true);
    }

    public Deque<VoidItem> getINSERT_QUEUE() {
        return INSERT_QUEUE;
    }
}

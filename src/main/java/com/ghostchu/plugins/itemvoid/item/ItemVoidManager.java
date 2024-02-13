package com.ghostchu.plugins.itemvoid.item;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class ItemVoidManager implements AutoCloseable {
    private Deque<VoidItem> INSERT_QUEUE = new ConcurrentLinkedDeque<>();
    private AtomicBoolean stopFilterThread = new AtomicBoolean(false);

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
            Set<BakedVoidItem> pool = new HashSet<>(n);
            int counter = 0;

            while (counter < n) {
                VoidItem voidItem = INSERT_QUEUE.pollFirst();
                if (voidItem == null) {
                    break;
                }
                if (isCollectItem(voidItem.getItemStack())) {
                    counter++;
                    pool.add(new BakedVoidItem(voidItem));
                }
                Collection<BakedVoidItem> stacks = parsePossibleExtraContent(voidItem.getItemStack().getItemMeta()).stream().map(item -> new BakedVoidItem(voidItem.getDiscoverAt(), item)).toList();
                counter += stacks.size();
                pool.addAll(stacks);
            }

            return pool;
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

    private Collection<ItemStack> parsePossibleExtraContent(ItemMeta meta) {
        try {
            Set<ItemStack> set = new HashSet<>();
            if (meta instanceof BlockStateMeta im) {
                if (im.getBlockState() instanceof InventoryHolder holder) {
                    for (ItemStack content : holder.getInventory().getContents()) {
                        if (content.hasItemMeta()) {
                            set.addAll(parsePossibleExtraContent(content.getItemMeta()));
                        } else {
                            set.add(content);
                        }
                    }
                }
            }
            return set;
        } catch (StackOverflowError error) {
            error.printStackTrace();
            return Set.of();
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

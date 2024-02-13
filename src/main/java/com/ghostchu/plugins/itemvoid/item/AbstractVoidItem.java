package com.ghostchu.plugins.itemvoid.item;

import org.bukkit.inventory.ItemStack;

public abstract class AbstractVoidItem implements VoidItem{
    public long discoverTime;
    public ItemStack stack;

    public AbstractVoidItem(long discoverTime, ItemStack stack){
        this.discoverTime = discoverTime;
        this.stack = stack;
    }

    @Override
    public ItemStack getItemStack() {
        return stack;
    }

    @Override
    public long getDiscoverAt() {
        return discoverTime;
    }

    public void setDiscoverTime(long discoverTime) {
        this.discoverTime = discoverTime;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }
}

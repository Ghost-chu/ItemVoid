package com.ghostchu.plugins.itemvoid.item;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class DatabaseItem {
    private long discoverTime;
    private long sha256;
    private String name;
    private String lore;
    private String nbt;
    private String bukkitSerialized;
    private String material;
    private ItemStack itemStack;

    public DatabaseItem(long discoverTime, long sha256, String name, String lore, String nbt, String bukkitSerialized, String material) throws InvalidConfigurationException {
        this.discoverTime = discoverTime;
        this.sha256 = sha256;
        this.name = name;
        this.lore = lore;
        this.nbt = nbt;
        this.bukkitSerialized = bukkitSerialized;
        this.material = material;
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(bukkitSerialized);
        this.itemStack = yaml.getItemStack("item");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseItem that = (DatabaseItem) o;
        return discoverTime == that.discoverTime && sha256 == that.sha256 && Objects.equals(name, that.name) && Objects.equals(lore, that.lore) && Objects.equals(nbt, that.nbt) && Objects.equals(bukkitSerialized, that.bukkitSerialized) && Objects.equals(material, that.material) && Objects.equals(itemStack, that.itemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(discoverTime, sha256, name, lore, nbt, bukkitSerialized, material, itemStack);
    }

    public void setNbt(String nbt) {
        this.nbt = nbt;
    }

    public String getNbt() {
        return nbt;
    }

    public long getDiscoverTime() {
        return discoverTime;
    }

    public void setDiscoverTime(long discoverTime) {
        this.discoverTime = discoverTime;
    }

    public long getSha256() {
        return sha256;
    }

    public void setSha256(long sha256) {
        this.sha256 = sha256;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLore() {
        return lore;
    }

    public void setLore(String lore) {
        this.lore = lore;
    }

    public String getBukkitSerialized() {
        return bukkitSerialized;
    }

    public void setBukkitSerialized(String bukkitSerialized) {
        this.bukkitSerialized = bukkitSerialized;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}

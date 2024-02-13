package com.ghostchu.plugins.itemvoid.item;

import com.google.common.hash.Hashing;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class BakedVoidItem extends AbstractVoidItem {
    private String nbt;
    private long sha256;
    private String name;
    private String lore;
    private String bukkitSerialized;
    private String material;

    public BakedVoidItem(VoidItem item) {
        this(item.getDiscoverAt(), item.getItemStack());
    }

    public BakedVoidItem(long discoverTime, ItemStack stack) {
        super(discoverTime, stack.clone());
        trimItemStack();
        readItemStack();
        serializeItemStack();
        calcHash();
    }

    private void readItemStack() {
        this.name = "<NULL-NULL-NULL>"; // 不给数据库 NULL 值，影响索引
        this.lore = "<NULL-NULL-NULL>"; // 不给数据库 NULL 值，影响索引
        this.material = stack.getType().getKey().toString();
        if (getItemStack().hasItemMeta()) {
            ItemMeta meta = getItemStack().getItemMeta();
            if (meta.hasDisplayName()) {
                this.name = ChatColor.stripColor(meta.getDisplayName());
            }
            if (meta.hasLore()) {
                StringJoiner joiner = new StringJoiner("\n");
                meta.getLore().forEach(l->joiner.add(ChatColor.stripColor(l)));
                this.lore = joiner.toString();
            }
        }
    }

    public String getMaterial() {
        return material;
    }

    public String getBukkitSerialized() {
        return bukkitSerialized;
    }

    @Override
    public long getDiscoverAt() {
        return super.getDiscoverAt();
    }

    public long getSha256() {
        return sha256;
    }

    public String getNbt() {
        return nbt;
    }

    public String getName() {
        return name;
    }

    public String getLore() {
        return lore;
    }

    @Override
    public ItemStack getItemStack() {
        return super.getItemStack();
    }

    private void calcHash() {
        sha256 = Hashing.sha256().hashString(material+" "+nbt, StandardCharsets.UTF_8).padToLong();
    }

    private void serializeItemStack() {
        NBTItem nbtItem = new NBTItem(stack);
        nbt = nbtItem.toString();
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("item",stack);
        bukkitSerialized = yaml.saveToString();
    }

    private void trimItemStack() {
        ItemStack trimmed = getItemStack().clone();
        trimmed.setAmount(1);
        if (trimmed.hasItemMeta()) {
            ItemMeta meta = trimmed.getItemMeta();
            if (meta instanceof Damageable damageable) {
                damageable.setDamage(0);
            }
            trimmed.setItemMeta(meta);
        }
        stack = trimmed;
    }
}

package de.luludodo.fivelives.features.item;

import de.luludodo.fivelives.FiveLives;
import de.luludodo.fivelives.config.Config;
import de.luludodo.fivelives.log.Log;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.Map;

import static de.luludodo.fivelives.config.translations.Translation.*;

public enum LifeItem {

    FRAGMENT("fragment", Material.GHAST_TEAR),
    LIFE("life", Material.NETHER_STAR),
    BEACON("beacon", Material.BEACON);

    private final NamespacedKey id = new NamespacedKey(FiveLives.getPlugin(), "id");

    private final ItemStack item;
    private final String itemId;
    LifeItem(@NotNull String id, @NotNull Material material) {
        this.itemId = id;
        item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.getPersistentDataContainer().set(this.id, PersistentDataType.STRING, id);
        itemMeta.setUnbreakable(true);
        itemMeta.addEnchant(Enchantment.DURABILITY, 0, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(itemMeta);
    }

    private void loadTranslations() {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(
                translation("item." + itemId + ".name")
                        .get(
                                false,
                                true,
                                false,
                                false,
                                false,
                                ChatColor.WHITE
                        )
        );
        itemMeta.setLore(
                translation("item." + itemId + ".lore")
                        .set("max_lives", Config.maxLives)
                        .set("revive_lives", Config.reviveLives)
                        .getAsList(
                                false,
                                false,
                                false,
                                false,
                                false,
                                ChatColor.WHITE
                        )
        );
        item.setItemMeta(itemMeta);
    }

    public @NotNull ItemStack get(int amount) {
        ItemStack clone = item.clone();
        clone.setAmount(amount);
        return clone;
    }

    public @NotNull String getId() {
        return itemId;
    }

    public boolean isThisItem(@Nullable ItemStack checkItem) {
        if (checkItem == null) {
            return false;
        }
        ItemMeta checkMeta = checkItem.getItemMeta();
        if (checkMeta == null) {
            return false;
        }
        return itemId.equals(checkMeta.getPersistentDataContainer().get(this.id, PersistentDataType.STRING));
    }

    public void giveTo(HumanEntity entity, int amount) {
        giveItemTo(get(amount), entity);
    }

    private static final NamespacedKey staticId = new NamespacedKey(FiveLives.getPlugin(), "id");
    private static final String[] ids;

    static {
        LifeItem[] values = values();
        ids = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            ids[i] = values[i].getId();
        }
    }

    public static boolean isCustomItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        String id = meta.getPersistentDataContainer().get(staticId, PersistentDataType.STRING);
        return id != null;
    }

    public static @Nullable String idOf(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(staticId, PersistentDataType.STRING);
    }

    public static @Nullable ItemStack getCustomItem(@NotNull String id, int amount) {
        for (LifeItem item:values()) {
            if (item.getId().equals(id)) {
                return item.get(amount);
            }
        }
        return null;
    }

    public static String[] getIds() {
        return ids.clone();
    }

    public static void giveItemTo(ItemStack item, HumanEntity entity) {
        Map<Integer, ItemStack> overflow = entity.getInventory().addItem(item);
        if (!overflow.isEmpty()) {
            entity.getWorld().dropItem(entity.getLocation(), overflow.get(0));
        }
        Log.info("Gave " + item.getAmount() + " " + itemToString(item) + " to " + entity.getName());
    }

    public static void loadAllTranslations() {
        for (LifeItem item:values()) {
            item.loadTranslations();
        }
    }

    public static ItemStack choiceToItem(RecipeChoice choice) {
        if (choice instanceof RecipeChoice.ExactChoice) {
            return  ((RecipeChoice.ExactChoice) choice).getItemStack();
        } else {
            return  ((RecipeChoice.MaterialChoice) choice).getItemStack();
        }
    }

    public static String itemToString(ItemStack item) {
        if (LifeItem.isCustomItem(item)) {
            return "five-lives:" + LifeItem.idOf(item);
        } else {
            return item.getType().getKey().toString();
        }
    }
}
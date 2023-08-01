package de.luludodo.fivelives.features.revive;

import de.luludodo.fivelives.FiveLives;
import de.luludodo.fivelives.api.LifeApi;
import de.luludodo.fivelives.config.Config;
import de.luludodo.fivelives.config.lives.LivesConfig;
import de.luludodo.fivelives.features.ban.BannedPreLoginChecker;
import de.luludodo.fivelives.features.gui.GUIManager;
import de.luludodo.fivelives.features.gui.ManagedInventory;
import de.luludodo.fivelives.features.item.LifeItem;
import de.luludodo.fivelives.features.send.FeedbackSender;
import de.luludodo.fivelives.log.Log;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static de.luludodo.fivelives.config.translations.Translation.*;

public class ReviveGUI {

    private static final NamespacedKey pageNamespace = new NamespacedKey(FiveLives.getPlugin(), "page");

    private static final ItemStack noBannedPlayerItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    static {
        ItemMeta noBannedPlayerMeta = noBannedPlayerItem.getItemMeta();
        noBannedPlayerMeta.setDisplayName(
                translation("gui.revive.noBannedPlayer.name")
                        .get(
                                false,
                                false,
                                false,
                                false,
                                false,
                                ChatColor.WHITE
                        )
        );
        noBannedPlayerMeta.setLore(
                translation("gui.revive.noBannedPlayer.lore")
                        .set("revive-lives", Config.reviveLives)
                        .getAsList(
                                false,
                                false,
                                false,
                                false,
                                false,
                                ChatColor.WHITE
                        )
        );
        noBannedPlayerMeta.addEnchant(Enchantment.DURABILITY, 0, true);
        noBannedPlayerMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        noBannedPlayerItem.setItemMeta(noBannedPlayerMeta);
    }

    private static final Function<Integer, ItemStack> previousArrow = page -> {
        ItemStack previousArrow = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previousArrow.getItemMeta();
        previousMeta.setDisplayName(
                translation("gui.revive.previousPage.name")
                        .set("page", page)
                        .get(
                                false,
                                false,
                                false,
                                false,
                                false,
                                ChatColor.WHITE
                        )
        );
        previousMeta.setLore(
                translation("gui.revive.previousPage.lore")
                        .set("page", page)
                        .getAsList(
                                false,
                                false,
                                false,
                                false,
                                false,
                                ChatColor.WHITE
                        )
        );
        previousMeta.addEnchant(Enchantment.DURABILITY, 0, true);
        previousMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        previousMeta.getPersistentDataContainer().set(pageNamespace, PersistentDataType.INTEGER, page - 1);
        previousArrow.setItemMeta(previousMeta);
        return previousArrow;
    };

    private static final Function<Integer, ItemStack> nextArrow = page -> {
        ItemStack nextArrow = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextArrow.getItemMeta();
        nextMeta.setDisplayName(
                translation("gui.revive.nextPage.name")
                        .set("page", page + 2)
                        .get(
                                false,
                                false,
                                false,
                                false,
                                false,
                                ChatColor.WHITE
                        )
        );
        nextMeta.setLore(
                translation("gui.revive.nextPage.lore")
                        .set("page", page + 2)
                        .getAsList(
                                false,
                                false,
                                false,
                                false,
                                false,
                                ChatColor.WHITE
                        )
        );
        nextMeta.addEnchant(Enchantment.DURABILITY, 0, true);
        nextMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        nextMeta.getPersistentDataContainer().set(pageNamespace, PersistentDataType.INTEGER, page + 1);
        nextArrow.setItemMeta(nextMeta);
        return nextArrow;
    };

    public static void open(HumanEntity player) {
        open(player, 0);
    }

    private static void open(HumanEntity player, int page) {
        Inventory inventory = Bukkit.createInventory(player, 54, translationAsString("gui.revive.title"));
        displayPage(inventory, page);
        ManagedInventory managedInventory = GUIManager.manage(inventory);
        managedInventory.setMoveItems(false);
        managedInventory.registerClickEvent(ReviveGUI::onInventoryClick);
        managedInventory.registerCloseEvent(ReviveGUI::onInventoryClose);
        player.openInventory(inventory);
        managedInventory.closeable();
    }

    // Page 0-based
    private static void displayPage(Inventory inventory, int page) {
        inventory.clear();
        if (page == -1) {
            inventory.setItem(22, noBannedPlayerItem);
            return;
        }
        AtomicInteger size = new AtomicInteger(0);
        List<UUID> bannedUUIDs = new ArrayList<>();
        LivesConfig.uuidToLifeInfo.forEach((uuid, info) -> {
            if (info.getLives() == 0) {
                size.incrementAndGet();
                bannedUUIDs.add(uuid);
            }
        });
        ItemsFeedback feedback = new ItemsFeedback(page, size.get());
        if (feedback.size < 1) {
            displayPage(inventory, page - 1);
            return;
        }
        if (feedback.previousPage) {
            inventory.setItem(45, previousArrow.apply(page));
        }
        if (feedback.nextPage) {
            inventory.setItem(53, nextArrow.apply(page));
        }
        inventory.addItem(getItems(bannedUUIDs, feedback.offset, feedback.items, page));
    }

    private final static NamespacedKey confirmNamespace = new NamespacedKey(FiveLives.getPlugin(), "confirm");
    private final static ItemStack confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
    static {
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName(
                translation("gui.revive.areYouSure.confirm")
                        .get(
                                false,
                                false,
                                false,
                                false,
                                false,
                                ChatColor.WHITE
                        )
        );
        confirmMeta.addEnchant(Enchantment.DURABILITY, 0, true);
        confirmMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        confirmMeta.getPersistentDataContainer().set(confirmNamespace, PersistentDataType.BOOLEAN, true);
        confirmItem.setItemMeta(confirmMeta);
    }
    private final static ItemStack cancelItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    static {
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(
                translation("gui.revive.areYouSure.cancel")
                        .get(
                                false,
                                false,
                                false,
                                false,
                                false,
                                ChatColor.WHITE
                        )
        );
        cancelMeta.addEnchant(Enchantment.DURABILITY, 0, true);
        cancelMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        cancelMeta.getPersistentDataContainer().set(confirmNamespace, PersistentDataType.BOOLEAN, false);
        cancelItem.setItemMeta(cancelMeta);
    }

    private static void areYouSure(HumanEntity player, ItemStack item) {
        Inventory inventory = Bukkit.createInventory(player, 27, translationAsString("gui.revive.areYouSure.title"));
        inventory.setItem(11, cancelItem);
        inventory.setItem(13, item);
        inventory.setItem(15, confirmItem);
        ManagedInventory managed = GUIManager.manage(inventory);
        managed.setMoveItems(false);
        managed.registerClickEvent(ReviveGUI::onAreYouSureInventoryClick);
        managed.registerCloseEvent(ReviveGUI::onInventoryClose);
        player.openInventory(inventory);
        managed.closeable();
    }

    private static void onAreYouSureInventoryClick(InventoryClickEvent event) {
        PersistentDataContainer data = event.getInventory().getItem(13).getItemMeta().getPersistentDataContainer();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) {
            return;
        }
        Boolean confirm = item.getItemMeta().getPersistentDataContainer().get(confirmNamespace, PersistentDataType.BOOLEAN);
        if (confirm == null) {
            return;
        }
        if (confirm) {
            String uuidString = data.get(uuidNamespace, PersistentDataType.STRING);
            UUID uuid = UUID.fromString(uuidString);
            close(event.getWhoClicked(), event.getInventory());
            FeedbackSender.sendApiResult(
                    event.getWhoClicked(),
                    translation("gui.revive.unban")
                            .set("player", getName(uuid))
                            .get(),
                    LifeApi.revive(uuid)
            );
        } else {
            open(event.getWhoClicked(), data.get(pageNamespace, PersistentDataType.INTEGER));
        }
    }

    private static final NamespacedKey uuidNamespace = new NamespacedKey(FiveLives.getPlugin(), "bannedUuid");
    private static void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) {
            return;
        }
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        if (item.getType() == Material.ARROW) {
            Integer page = data.get(pageNamespace, PersistentDataType.INTEGER);
            if (page != null) {
                displayPage(event.getInventory(), page);
            }
        } else if (item.getType() == Material.PLAYER_HEAD) {
            areYouSure(event.getWhoClicked(), item);
        }
    }

    private static final ItemStack noRefundItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    private static final NamespacedKey noRefundKey = new NamespacedKey(FiveLives.getPlugin(), "noRefund");
    static {
        noRefundItem.getItemMeta().getPersistentDataContainer().set(noRefundKey, PersistentDataType.BOOLEAN, true);
    }
    private static void close(HumanEntity entity, Inventory inventory) {
        inventory.setItem(0, noRefundItem);
        entity.closeInventory();
    }

    private static void onInventoryClose(InventoryCloseEvent event) {
        if (Objects.equals(event.getInventory().getItem(0), noRefundItem)) {
            PlayerInventory inventory = event.getPlayer().getInventory();
            ItemStack itemMainHand = event.getPlayer().getInventory().getItemInMainHand();
            ItemStack itemOffHand = event.getPlayer().getInventory().getItemInOffHand();
            if (LifeItem.BEACON.isThisItem(itemMainHand)) {
                inventory.setItemInMainHand(LifeItem.BEACON.get(itemMainHand.getAmount() - 1));
            } else if (LifeItem.BEACON.isThisItem(itemOffHand)) {
                inventory.setItemInOffHand(LifeItem.BEACON.get(itemOffHand.getAmount() - 1));
            } else {
                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    ItemStack item = inventory.getItem(slot);
                    if (LifeItem.BEACON.isThisItem(item)) {
                        item.setAmount(item.getAmount() - 1);
                        inventory.setItem(slot, item);
                        return;
                    }
                };
                Log.err("Couldn't remove five-lives:beacon from " + event.getPlayer().getName());
            }
        }
    }

    private static class ItemsFeedback {
        final boolean nextPage;
        final boolean previousPage;
        final int items;
        final int size;
        final int offset;
        ItemsFeedback(int page, int size) {
            int offset = 0;
            for (int p = 0; p < page; p++) {
                offset += p > 0? 52 : 53;
            }
            this.offset = offset;
            this.size = size - offset;
            previousPage = page > 0;
            nextPage = previousPage? this.size > 53 : this.size > 54;
            items = previousPage? (nextPage? 52 : 53) : (nextPage? 53 : 54);
        }
    }

    public static ItemStack[] getItems(List<UUID> bannedUUIDs, int offset, int amount, int page) {
        amount = Math.min(amount, bannedUUIDs.size() - offset);
        bannedUUIDs.sort(
                (uuid1, uuid2) -> String.CASE_INSENSITIVE_ORDER.compare(
                        getName(uuid1),
                        getName(uuid2)
                )
        );
        ItemStack[] items = new ItemStack[amount];
        for (int index = 0; index < amount; index++) {
            UUID uuid = bannedUUIDs.get(index + offset);
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(process(getName(uuid), false, false, false, false, false, ChatColor.GOLD));
            meta.setLore(stringToList("\n" + process(BannedPreLoginChecker.banMessage(uuid), false, false, false, false, false, ChatColor.WHITE)));
            ((SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            meta.getPersistentDataContainer().set(uuidNamespace, PersistentDataType.STRING, uuid.toString());
            meta.getPersistentDataContainer().set(pageNamespace, PersistentDataType.INTEGER, page);
            item.setItemMeta(meta);
            items[index] = item;
        }
        return items;
    }

    private static @NotNull String getName(@Nullable UUID uuid) {
        if (uuid == null) {
            return "null";
        } else {
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) {
                return uuid.toString();
            } else {
                return name;
            }
        }
    }
}

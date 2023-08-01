package de.luludodo.fivelives.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class GUIManager implements Listener {
    private static final Map<Inventory, ManagedInventory> inventoryToManaged = new HashMap<>();

    public static ManagedInventory manage(Inventory inventory) {
        ManagedInventory managedInventory = new ManagedInventory();
        inventoryToManaged.put(inventory, managedInventory);
        return managedInventory;
    }

    @EventHandler
    private static void onInventoryCloseEvent(InventoryCloseEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        ManagedInventory managed = inventoryToManaged.get(inventory);
        if (managed == null) {
            return;
        }
        managed.consumeClose(event);
        if (managed.isCloseable() && inventory.getViewers().size() <= 1) {
            inventoryToManaged.remove(inventory);
        }
    }

    @EventHandler
    private static void onInventoryClickEvent(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        ManagedInventory managed = inventoryToManaged.get(inventory);
        if (managed == null) {
            return;
        }
        managed.consumeClick(event);
        if (!managed.getMoveItems()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private static void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        ManagedInventory managed = inventoryToManaged.get(inventory);
        if (managed == null) {
            return;
        }
        if (!managed.getMoveItems()) {
            event.setCancelled(true);
        }
    }

    public static void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            InventoryCloseEvent closeEvent = new InventoryCloseEvent(player.getOpenInventory());
            onInventoryCloseEvent(closeEvent);
            player.closeInventory();
        });
    }
}

package de.luludodo.fivelives.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ManagedInventory {
    private final List<Consumer<InventoryCloseEvent>> closeEvents = new ArrayList<>(0);
    private final List<Consumer<InventoryClickEvent>> clickEvents = new ArrayList<>(0);
    private boolean closeable = false;
    private boolean moveItems = true;
    ManagedInventory() {}

    public void registerCloseEvent(Consumer<InventoryCloseEvent> eventConsumer) {
        closeEvents.add(eventConsumer);
    }

    public void registerClickEvent(Consumer<InventoryClickEvent> eventConsumer) {
        clickEvents.add(eventConsumer);
    }

    public void closeable() {
        closeable = true;
    }

    public boolean isCloseable() {
        return closeable;
    }

    public void setMoveItems(boolean moveItems) {
        this.moveItems = moveItems;
    }

    public boolean getMoveItems() {
        return moveItems;
    }

    void consumeClose(InventoryCloseEvent event) {
        closeEvents.forEach(eventConsumer -> eventConsumer.accept(event));
    }

    void consumeClick(InventoryClickEvent event) {
        clickEvents.forEach(eventConsumer -> eventConsumer.accept(event));
    }
}
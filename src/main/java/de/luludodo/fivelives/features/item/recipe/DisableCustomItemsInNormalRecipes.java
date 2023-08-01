package de.luludodo.fivelives.features.item.recipe;

import de.luludodo.fivelives.features.item.LifeItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

import java.util.Arrays;

public class DisableCustomItemsInNormalRecipes implements Listener {
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!LifeItem.isCustomItem(event.getRecipe().getResult())) {
            Arrays.stream(event.getInventory().getMatrix()).forEach((item) -> {
                if (LifeItem.isCustomItem(item)) {
                    event.setCancelled(true);
                }
            });
        }
    }
}

package de.luludodo.fivelives.features.item.recipe;

import de.luludodo.fivelives.features.gui.GUIManager;
import de.luludodo.fivelives.features.gui.ManagedInventory;
import de.luludodo.fivelives.features.item.LifeItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static de.luludodo.fivelives.config.translations.Translation.*;

public class RecipeGUI {

    public static void open(HumanEntity player, LifeRecipe recipe) {
        Inventory inventory = Bukkit.createInventory(
                player,
                InventoryType.WORKBENCH,
                translation("gui.recipe.title")
                        .set("item", translationAsString("gui.recipe.item." + LifeItem.idOf(recipe.recipe.getResult())))
                        .get()
        );
        int i = 1;
        for (ItemStack item:getItems(recipe.recipe)) {
            inventory.setItem(i, item);
            i++;
        }
        inventory.setItem(0, recipe.recipe.getResult());
        ManagedInventory managedInventory = GUIManager.manage(inventory);
        managedInventory.setMoveItems(false);
        player.openInventory(inventory);
        managedInventory.closeable();
    }

    private static ItemStack[] getItems(Recipe recipe) {
        ItemStack[] items;
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            Map<Character, ItemStack> ingredient = shapedRecipe.getIngredientMap();
            Map<Character, RecipeChoice> choices = shapedRecipe.getChoiceMap();
            String[] shape = shapedRecipe.getShape();
            items = new ItemStack[9];
            int i = 0;
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    char c = shape[x].charAt(y);
                    if (ingredient.containsKey(c)) {
                        items[i] = ingredient.get(c);
                    } else {
                        RecipeChoice curChoice = choices.get(c);
                        if (curChoice instanceof RecipeChoice.ExactChoice) {
                            items[i] = ((RecipeChoice.ExactChoice) curChoice).getItemStack();
                        } else {
                            items[i] = ((RecipeChoice.MaterialChoice) curChoice).getItemStack();
                        }
                    }
                    i++;
                }
            }
        } else {
            ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
            List<ItemStack> itemList = shapelessRecipe.getIngredientList();
            List<RecipeChoice> choiceList = shapelessRecipe.getChoiceList();
            items = new ItemStack[itemList.size() + choiceList.size()];
            AtomicInteger i = new AtomicInteger(0);
            itemList.forEach(item -> {
                items[i.get()] = item;
                i.incrementAndGet();
            });
            choiceList.forEach(choice -> {
                items[i.get()] = LifeItem.choiceToItem(choice);
                i.incrementAndGet();
            });
        }
        return items;
    }
}

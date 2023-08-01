package de.luludodo.fivelives.features.item.recipe;

import de.luludodo.fivelives.FiveLives;
import de.luludodo.fivelives.features.item.LifeItem;
import de.luludodo.fivelives.log.Log;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LifeRecipe {
    public static class RecipeFormatException extends IllegalArgumentException {
        public RecipeFormatException(String s) {
            super(s);
        }
    }

    private static final Map<String, LifeRecipe> idToRecipe = new HashMap<>();

    public static LifeRecipe getOrCreateInstance(String id) {
        if (idToRecipe.containsKey(id)) {
            return idToRecipe.get(id);
        } else {
            return new LifeRecipe(id);
        }
    }

    public static LifeRecipe getInstance(String id) {
        return idToRecipe.get(id);
    }

    public static Set<String> getIds() {
        return idToRecipe.keySet();
    }

    private static final List<NamespacedKey> recipes = new ArrayList<>();
    public static List<NamespacedKey> getRecipes() {
        return recipes;
    }

    private final String id;
    private final NamespacedKey namespacedKey;
    protected Recipe recipe;
    private LifeRecipe(String id) {
        this.id = id;
        namespacedKey = new NamespacedKey(FiveLives.getPlugin(), id);
        recipes.add(namespacedKey);
        idToRecipe.put(id, this);
    }

    public void load(ConfigurationSection section) {
        if (section == null) {
            throw new RecipeFormatException("Recipe is null");
        }
        this.recipe = null;
        Bukkit.removeRecipe(namespacedKey);
        boolean shaped = section.getBoolean("shaped", true);
        ItemStack result = LifeItem.getCustomItem(id, 1);
        if (result == null) {
            throw new RecipeFormatException("Recipe for invalid item");
        }
        if (shaped) {
            ShapedRecipe recipe = new ShapedRecipe(
                    namespacedKey,
                    result
            );
            List<String> shapeList = section.getStringList("shape");
            if (shapeList.size() != 3) {
                throw new RecipeFormatException("Invalid shape");
            }
            Set<Character> chars = new HashSet<>(9);
            String[] shape = new String[3];
            for (int line = 0; line < 3; line++) {
                String shapePart = shapeList.get(line);
                if (shapePart.length() != 3) {
                    throw new RecipeFormatException("Invalid shape");
                }
                for (int charId = 0; charId < 3; charId++) {
                    chars.add(shapePart.charAt(charId));
                }
                shape[line] = shapeList.get(line);
            }
            recipe.shape(shape);
            for (String key:section.getKeys(false)) {
                if (key.length() == 1) {
                    char keyChar = key.charAt(0);
                    if (!chars.remove(keyChar)) {
                        throw new RecipeFormatException("Invalid key '" + key + "'");
                    }
                    String materialString = section.getString(key, "");
                    if (materialString.startsWith("five-lives:")) {
                        ItemStack item = LifeItem.getCustomItem(materialString.substring(11), 1);
                        if (item == null) {
                            throw new RecipeFormatException(
                                    "Invalid material (" + materialString + ") for key '" + key + "'"
                            );
                        }
                        recipe.setIngredient(keyChar, new RecipeChoice.ExactChoice(item));
                    } else {
                        Material material = Material.matchMaterial(materialString);
                        if (material == null) {
                            throw new RecipeFormatException(
                                    "Invalid material (" + materialString + ") for key '" + key + "'"
                            );
                        }
                        recipe.setIngredient(keyChar, material);
                    }
                }
            }
            if (!chars.isEmpty()) {
                throw new RecipeFormatException("One or more keys are missing! " + chars);
            }
            this.recipe = recipe;
            Bukkit.addRecipe(recipe);
        } else {
            ShapelessRecipe recipe = new ShapelessRecipe(
                    namespacedKey,
                    result
            );
            int keys = 0;
            for (String key:section.getKeys(false)) {
                if (key.length() == 1) {
                    keys++;
                    Material material = Material.matchMaterial(section.getString(key, ""));
                    if (material == null) {
                        throw new RecipeFormatException(
                                "Invalid material (" + section.getString(key) + ") for key '" + key + "'"
                        );
                    }
                    recipe.addIngredient(material);
                }
            }
            if (keys > 9) {
                throw new RecipeFormatException("Too many ingredients (" + keys + ")");
            }
            this.recipe = recipe;
            Bukkit.addRecipe(recipe);
        }
    }

    public void save(ConfigurationSection section) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            section.set("shaped", true);
            section.set("shape", Arrays.asList(shapedRecipe.getShape()));
            shapedRecipe.getIngredientMap().forEach(
                    (key, item) -> section.set(key.toString(), LifeItem.itemToString(item))
            );
            shapedRecipe.getChoiceMap().forEach(
                    (key, choice) -> section.set(key.toString(), LifeItem.itemToString(LifeItem.choiceToItem(choice)))
            );
        } else {
            ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
            section.set("shaped", false);
            AtomicInteger i = new AtomicInteger(0);
            shapelessRecipe.getIngredientList().forEach(item -> {
                section.set(String.valueOf(i.get()), LifeItem.itemToString(item));
                i.incrementAndGet();
            });
            shapelessRecipe.getChoiceList().forEach(choice -> {
                section.set(String.valueOf(i.get()), LifeItem.itemToString(LifeItem.choiceToItem(choice)));
                i.incrementAndGet();
            });
        }
    }
}

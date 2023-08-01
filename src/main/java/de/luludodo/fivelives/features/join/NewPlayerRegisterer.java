package de.luludodo.fivelives.features.join;

import de.luludodo.fivelives.api.LifeApi;
import de.luludodo.fivelives.features.item.recipe.LifeRecipe;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NewPlayerRegisterer implements Listener {
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        LifeApi.updateTab(event.getPlayer());
        LifeApi.handleApi(() -> LifeApi.updateEffects(event.getPlayer()), event.getPlayer());
        event.getPlayer().discoverRecipes(LifeRecipe.getRecipes());
    }
}

package de.luludodo.fivelives.features.death;

import de.luludodo.fivelives.api.LifeApi;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        LifeApi.handleApi(() -> LifeApi.death(event.getEntity(), event.getDeathMessage()), event.getEntity());
        event.setDeathMessage(null);
    }
}
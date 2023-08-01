package de.luludodo.fivelives.features.effects;

import de.luludodo.fivelives.FiveLives;
import de.luludodo.fivelives.api.LifeApi;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {
    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(
                FiveLives.getPlugin(),
                () -> LifeApi.handleApi(
                        () -> LifeApi.updateEffects(
                                event.getPlayer()
                        ),
                        event.getPlayer()
                ),
                1
        );
    }
}

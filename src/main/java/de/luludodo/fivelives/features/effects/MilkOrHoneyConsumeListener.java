package de.luludodo.fivelives.features.effects;

import de.luludodo.fivelives.FiveLives;
import de.luludodo.fivelives.api.LifeApi;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class MilkOrHoneyConsumeListener implements Listener {
    @EventHandler
    private void onMilkOrHoneyConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET || event.getItem().getType() == Material.HONEY_BOTTLE) {
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
}

package de.luludodo.fivelives.features.ban;

import de.luludodo.fivelives.api.LifeApi;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static de.luludodo.fivelives.config.translations.Translation.*;

public class BanLeaveMessageAndClear implements Listener {
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        if (LifeApi.get(event.getPlayer().getUniqueId()) == 0) {
            event.getPlayer().getInventory().clear();
            event.setQuitMessage(
                    translation("ban.leave")
                            .set("player", event.getPlayer().getName())
                            .get()
            );
        }
    }
}

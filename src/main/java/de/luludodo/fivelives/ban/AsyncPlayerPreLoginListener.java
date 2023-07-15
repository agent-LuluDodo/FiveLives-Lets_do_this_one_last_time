package de.luludodo.fivelives.ban;

import de.luludodo.fivelives.api.LifeApi;
import de.luludodo.fivelives.config.LifeConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AsyncPlayerPreLoginListener implements Listener {
    private static final LifeConfig config = LifeConfig.getInstance();

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (LifeApi.get(event.getUniqueId()) == 0) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, );
        }
    }
}

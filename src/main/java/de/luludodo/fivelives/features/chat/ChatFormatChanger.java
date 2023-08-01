package de.luludodo.fivelives.features.chat;

import de.luludodo.fivelives.api.LifeApi;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatFormatChanger implements Listener {
    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        event.setFormat(LifeApi.getPrefix(event.getPlayer().getUniqueId()) + event.getFormat());
    }
}

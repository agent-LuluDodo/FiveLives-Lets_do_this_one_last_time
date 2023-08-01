package de.luludodo.fivelives.features.revive;

import de.luludodo.fivelives.api.ApiResult;
import de.luludodo.fivelives.api.LifeApi;
import de.luludodo.fivelives.config.Config;
import de.luludodo.fivelives.config.lives.LivesConfig;
import de.luludodo.fivelives.features.item.LifeItem;
import de.luludodo.fivelives.features.send.FeedbackSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

import static de.luludodo.fivelives.config.translations.Translation.*;

public class UseReviveListener implements Listener {

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        if (LifeItem.BEACON.isThisItem(event.getItemInHand())) {
            event.setCancelled(true);
            ReviveGUI.open(event.getPlayer());
        }
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (LifeItem.LIFE.isThisItem(item)) {
                UUID uuid = event.getPlayer().getUniqueId();
                if (LifeApi.get(uuid) < Config.maxLives) {
                    ApiResult result = LifeApi.add(event.getPlayer().getUniqueId(), 1);
                    if (result == ApiResult.SUCCESS) {
                        item.setAmount(item.getAmount() - 1);
                    }
                    FeedbackSender.sendApiResult(event.getPlayer(), translationAsString("use.life.success"), result);
                } else {
                    FeedbackSender.sendError(
                            event.getPlayer(),
                            translation("use.life.error")
                                    .set("max-lives", Config.maxLives)
                                    .get()
                    );
                }
            } else if (LifeItem.BEACON.isThisItem(item)) {
                ReviveGUI.open(event.getPlayer());
            }
        }
    }
}

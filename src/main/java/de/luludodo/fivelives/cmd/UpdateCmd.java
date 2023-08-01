package de.luludodo.fivelives.cmd;

import de.luludodo.fivelives.features.item.LifeItem;
import de.luludodo.fivelives.features.send.FeedbackSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;

import static de.luludodo.fivelives.config.translations.Translation.*;

public class UpdateCmd extends LuluTabExecutor {
    @Override
    protected void command(@NonNull FeedbackSender feedback, @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        Player player = requirePlayer(sender, false);
        Map<String, Integer> updated = new HashMap<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = inventory.getItem(slot);
            String id = LifeItem.idOf(item);
            if (id != null) {
                updated.put(id, updated.getOrDefault(id, 0) + 1);
                inventory.setItem(slot, LifeItem.getCustomItem(id, item.getAmount()));
            }
        }
        if (updated.isEmpty()) {
            feedback.sendError(translationAsString("cmd.update.error"));
        } else {
            updated.forEach((id, amount) -> {
                feedback.sendSuccess(translation("cmd.update.success").set("id", id).set("amount", amount).get());
            });
        }
    }

    @Override
    protected void tabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {}
}

package de.luludodo.fivelives.cmd;

import de.luludodo.fivelives.api.ApiResult;
import de.luludodo.fivelives.api.LifeApi;
import de.luludodo.fivelives.features.item.LifeItem;
import de.luludodo.fivelives.features.send.FeedbackSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import static de.luludodo.fivelives.config.translations.Translation.*;

public class WithdrawCmd extends LuluTabExecutor {
    @Override
    public void command(@NonNull FeedbackSender feedback, @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        Player player = requirePlayer(sender, false);
        if (player == null) {
            return;
        }
        if (hasArgs(1)) {
            Integer lives = getInt(0, 1, LifeApi.get(player.getUniqueId()) - 1);
            if (lives == null) {
                return;
            }
            ApiResult result = LifeApi.remove(player.getUniqueId(), lives);
            if (result == ApiResult.SUCCESS) {
                LifeItem.LIFE.giveTo(player, lives);
                feedback.sendSuccess(
                        translation("cmd.withdraw.success")
                                .set("lives", lives)
                                .get()
                );
            } else {
                feedback.sendInternalError(result.getMessage());
            }
        }
    }

    @Override
    public void tabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (args.length == 1) {
            Player player = requirePlayer(sender, true);
            if (player != null) {
                addInts(1, LifeApi.get(player.getUniqueId()) - 1);
            }
        }
    }
}

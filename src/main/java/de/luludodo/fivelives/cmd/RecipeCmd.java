package de.luludodo.fivelives.cmd;

import de.luludodo.fivelives.features.item.recipe.LifeRecipe;
import de.luludodo.fivelives.features.item.recipe.RecipeGUI;
import de.luludodo.fivelives.features.send.FeedbackSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import static de.luludodo.fivelives.config.translations.Translation.*;

public class RecipeCmd extends LuluTabExecutor {

    @Override
    protected void command(@NonNull FeedbackSender feedback, @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        Player player = requirePlayer(sender, false);
        if (player == null) {
            return;
        }
        if (hasArgs(1)) {
            LifeRecipe recipe = LifeRecipe.getInstance(args[0]);
            if (recipe == null) {
                feedback.sendError(0, translation("cmd.recipe.error").set("id", args[0]).get());
                return;
            }
            RecipeGUI.open(player, recipe);
        }
    }

    @Override
    protected void tabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        Player player = requirePlayer(sender, true);
        if (player == null) {
            return;
        }
        if (args.length == 1) {
            addAll(LifeRecipe.getIds());
            addTitle("recipe", false);
        }
    }
}

package de.luludodo.fivelives.cmd;

import de.luludodo.fivelives.api.LifeApi;
import de.luludodo.fivelives.config.Config;
import de.luludodo.fivelives.config.LuluConfig;
import de.luludodo.fivelives.config.translations.Translations;
import de.luludodo.fivelives.features.item.LifeItem;
import de.luludodo.fivelives.features.send.FeedbackSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

import static de.luludodo.fivelives.config.translations.Translation.*;

public class LifeCmd extends LuluTabExecutor {
    @Override
    protected void command(@NonNull FeedbackSender feedback, @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (hasMinArgs(1)) {
            switch (args[0]) {
                case "get" -> {
                    if (hasArgs(2)) {
                        UUID uuid = getUUID(1);
                        feedback.sendSuccess(
                                translation("cmd.life.get.success")
                                        .set("name", getName(uuid))
                                        .set("lives", LifeApi.get(uuid))
                                        .get()
                        );
                    }
                }
                case "add" -> {
                    if (hasArgs(3)) {
                        UUID uuid = getUUID(1);
                        Integer lives = getInt(2, 1, Config.maxLives - LifeApi.get(uuid));
                        if (lives == null) {
                            return;
                        }
                        feedback.sendApiResult(
                                translation("cmd.life.add.success")
                                        .set("name", getName(uuid))
                                        .set("lives", lives)
                                        .get(),
                                LifeApi.add(uuid, lives));
                    }
                }
                case "remove" -> {
                    if (hasArgs(3)) {
                        UUID uuid = getUUID(1);
                        Integer lives = getInt(2, 1, LifeApi.get(uuid));
                        if (lives == null) {
                            return;
                        }
                        feedback.sendApiResult(
                                translation("cmd.life.remove.success")
                                        .set("name", getName(uuid))
                                        .set("lives", lives)
                                        .get(),
                                LifeApi.remove(uuid, lives)
                        );
                    }
                }
                case "set" -> {
                    if (hasArgs(3)) {
                        UUID uuid = getUUID(1);
                        Integer lives = getInt(2, 0, Config.maxLives);
                        if (lives == null) {
                            return;
                        }
                        feedback.sendApiResult(
                                translation("cmd.life.set.success")
                                        .set("name", getName(uuid))
                                        .set("lives", lives)
                                        .get(),
                                LifeApi.set(uuid, lives)
                        );
                    }
                }
                case "ban" -> {
                    if (hasArgs(2)) {
                        UUID uuid = getUUID(1);
                        if (LifeApi.get(uuid) == 0) {
                            feedback.sendError(
                                    1,
                                    translationAsString("cmd.life.ban.error")
                            );
                            return;
                        }
                        feedback.sendApiResult(
                                translation("cmd.life.ban.success")
                                        .set("name", getName(uuid))
                                        .get(),
                                LifeApi.set(uuid, 0)
                        );
                    }
                }
                case "move" -> {
                    if (hasArgs(4)) {
                        UUID from = getUUID(1);
                        UUID to = getUUID(2);
                        Integer lives = getInt(
                                3,
                                1,
                                Math.min(LifeApi.get(from), Config.maxLives - LifeApi.get(to))
                        );
                        if (lives == null) {
                            break;
                        }
                        feedback.sendApiResult(
                                translation("cmd.life.move.success")
                                        .set("lives", lives)
                                        .set("from", from)
                                        .set("to", to)
                                        .get(),
                                LifeApi.remove(from, lives),
                                LifeApi.add(to, lives)
                        );
                    }
                }
                case "revive" -> {
                    if (hasArgs(2)) {
                        UUID uuid = getUUID(1);
                        if (LifeApi.get(uuid) != 0) {
                            feedback.sendError(
                                    1,
                                    translationAsString("cmd.life.revive.error")
                            );
                            return;
                        }
                        feedback.sendApiResult(
                                translation("cmd.life.revive.success")
                                        .set("name", getName(uuid))
                                        .get(),
                                LifeApi.revive(uuid)
                        );
                    }
                }
                case "item" -> {
                    if (hasArgs(2, 3)) {
                        int amount = 1;
                        if (args.length == 3) {
                            Integer argAmount = getInt(2, 1, 64);
                            if (argAmount == null) {
                                return;
                            }
                            amount = argAmount;
                        }
                        Player player = requirePlayer(sender, false);
                        if (player == null) {
                            return;
                        }
                        ItemStack item = LifeItem.getCustomItem(args[1], amount);
                        if (item == null) {
                            feedback.sendError(
                                    1,
                                    translation("cmd.life.item.error")
                                            .set("id", args[1])
                                            .get()
                            );
                            return;
                        }
                        LifeItem.giveItemTo(item, player);
                        feedback.sendSuccess(
                                translation("cmd.life.item.success")
                                        .set("id", args[1])
                                        .set("amount", amount)
                                        .get()
                        );
                    }
                }
                case "language" -> {
                    if (hasArgs(2)) {
                        if (Translations.getInstance().setLanguage(args[1])) {
                            feedback.sendSuccess(
                                    translation("cmd.life.language.success")
                                            .set("lang", translationAsString("language"))
                                            .get()
                            );
                        } else {
                            feedback.sendError(
                                    1,
                                    translation("cmd.life.language.error")
                                            .set("lang", args[1])
                                            .get()
                            );
                        }
                    }
                }
                case "reload" -> {
                    if (hasArgs(1, 2)) {
                        if (args.length == 1) {
                            LuluConfig.forEach(config -> {
                                if (config.load()) {
                                    feedback.sendSuccess(
                                            translation("cmd.life.save.success")
                                                    .set("file", config.getName() + ".yml")
                                                    .get()
                                    );
                                } else {
                                    feedback.sendInternalError(
                                            translation("cmd.life.save.error")
                                                    .set("file", config.getName() + ".yml")
                                                    .get()
                                    );
                                }
                            });
                        } else {
                            LuluConfig config = LuluConfig.getInstance(args[1]);
                            if (config == null) {
                                feedback.sendError(
                                        1,
                                        translation("cmd.life.reset.error2")
                                                .set("file", args[1])
                                                .get()
                                );
                                return;
                            }
                            if (config.load()) {
                                feedback.sendSuccess(
                                        translation("cmd.life.save.success")
                                                .set("file", args[1] + ".yml")
                                                .get()
                                );
                            } else {
                                feedback.sendInternalError(
                                        translation("cmd.life.save.error")
                                                .set("file", args[1] + ".yml")
                                                .get()
                                );
                            }
                        }
                    }
                }
                case "save" -> {
                    if (hasArgs(1, 2)) {
                        if (args.length == 1) {
                            LuluConfig.forEach(config -> {
                                if (config.save()) {
                                    feedback.sendSuccess(
                                            translation("cmd.life.save.success")
                                                    .set("file", config.getName() + ".yml")
                                                    .get()
                                    );
                                } else {
                                    feedback.sendInternalError(
                                            translation("cmd.life.save.error")
                                                    .set("file", config.getName() + ".yml")
                                                    .get()
                                    );
                                }
                            });
                        } else {
                            LuluConfig config = LuluConfig.getInstance(args[1]);
                            if (config == null) {
                                feedback.sendError(
                                        1,
                                        translation("cmd.life.reset.error2")
                                                .set("file", args[1])
                                                .get()
                                );
                                return;
                            }
                            if (config.save()) {
                                feedback.sendSuccess(
                                        translation("cmd.life.save.success")
                                                .set("file", args[1] + ".yml")
                                                .get()
                                );
                            } else {
                                feedback.sendInternalError(
                                        translation("cmd.life.save.error")
                                                .set("file", args[1] + ".yml")
                                                .get()
                                );
                            }
                        }
                    }
                }
                case "reset" -> {
                    if (hasArgs(1, 2)) {
                        if (args.length == 1) {
                            LuluConfig.forEach(config -> {
                                config.reset();
                                feedback.sendSuccess(
                                        translation("cmd.life.reset.success")
                                                .set("file", config.getName() + ".yml")
                                                .get()
                                );
                            });
                        } else {
                            LuluConfig config = LuluConfig.getInstance(args[1]);
                            if (config == null) {
                                feedback.sendError(
                                        1,
                                        translation("cmd.life.reset.error")
                                                .set("file", args[1])
                                                .get()
                                );
                                return;
                            }
                            config.reset();
                            feedback.sendSuccess(
                                    translation("cmd.life.reset.success")
                                            .set("file", args[1] + ".yml")
                                            .get()
                            );
                        }
                    }
                }
                default -> feedback.sendError(0, translationAsString("cmd.error.notAOption"));
            }
        }
    }

    @Override
    protected void tabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        boolean isPlayer = requirePlayer(sender, true) != null;
        if (args.length == 1) {
            add("get");
            add("add");
            add("remove");
            add("set");
            add("ban");
            add("move");
            add("revive");
            if (isPlayer) {
                add("item");
            }
            add("language");
            add("reload");
            add("save");
            add("reset");
            addTitle("action", false);
        } else {
            switch (args[0]) {
                case "get" -> {
                    if (args.length == 2) {
                        getAllPlayerNames().forEach(this::add);
                        addTitle("player", false);
                    }
                }
                case "add" -> {
                    if (args.length == 2) {
                        getAllPlayerNames().forEach(this::add);
                        addTitle("player", false);
                    } else if (args.length == 3) {
                        addInts(1, Config.maxLives - LifeApi.get(getUUID(1)));
                        addTitle("lives", false);
                    }
                }
                case "remove" -> {
                    if (args.length == 2) {
                        getAllPlayerNames().forEach(this::add);
                        addTitle("player", false);
                    } else if (args.length == 3) {
                        addInts(1, LifeApi.get(getUUID(1)));
                        addTitle("lives", false);
                    }
                }
                case "set" -> {
                    if (args.length == 2) {
                        getAllPlayerNames().forEach(this::add);
                        addTitle("player", false);
                    } else if (args.length == 3) {
                        addInts(0, Config.maxLives);
                        addTitle("lives", false);
                    }
                }
                case "ban" -> {
                    if (args.length == 2) {
                        getAllPlayerNames(player -> LifeApi.get(player.getUniqueId()) > 0).forEach(this::add);
                        addTitle("player", false);
                    }
                }
                case "move" -> {
                    if (args.length == 2) {
                        getAllPlayerNames().forEach(this::add);
                        addTitle("from", false);
                    } else if (args.length == 3) {
                        getAllPlayerNames().forEach(this::add);
                        addTitle("to", false);
                    } else if (args.length == 4) {
                        addInts(1, Math.min(LifeApi.get(getUUID(1)), Config.maxLives - LifeApi.get(getUUID(2))));
                        addTitle("lives", false);
                    }
                }
                case "revive" -> {
                    if (args.length == 2) {
                        getAllPlayerNames(player -> LifeApi.get(player.getUniqueId()) == 0).forEach(this::add);
                        addTitle("player", false);
                    }
                }
                case "item" -> {
                    if (isPlayer) {
                        if (args.length == 2) {
                            addAll(LifeItem.getIds());
                            addTitle("item", false);
                        } else if (args.length == 3) {
                            addInts(1, 64);
                            addTitle("amount", true);
                        }
                    }
                }
                case "language" -> {
                    if (args.length == 2) {
                        addAll(Translations.getInstance().getLanguages());
                        addTitle("language", false);
                    }
                }
                case "reload", "save", "reset" -> {
                    if (args.length == 2) {
                        addAll(LuluConfig.getNames());
                        addTitle("file", true);
                    }
                }
            }
        }
    }
}

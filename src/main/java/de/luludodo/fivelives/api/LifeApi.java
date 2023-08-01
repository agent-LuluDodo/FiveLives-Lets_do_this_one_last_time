package de.luludodo.fivelives.api;

import de.luludodo.fivelives.FiveLives;
import de.luludodo.fivelives.config.Config;
import de.luludodo.fivelives.config.lives.LifeInfo;
import de.luludodo.fivelives.features.ban.BannedPreLoginChecker;
import de.luludodo.fivelives.features.ban.NoLifeInfo;
import de.luludodo.fivelives.config.lives.LivesConfig;
import de.luludodo.fivelives.features.item.LifeItem;
import de.luludodo.fivelives.log.Log;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;
import java.util.function.Supplier;

import static de.luludodo.fivelives.config.translations.Translation.*;

public class LifeApi {

    public static int get(UUID uuid) {
        LifeInfo info = LivesConfig.uuidToLifeInfo.get(uuid);
        return info == null? Config.maxLives : info.getLives();
    }

    public static ApiResult add(UUID uuid, int lives) {
        return set(uuid, get(uuid) + lives);
    }

    public static ApiResult remove(UUID uuid, int lives) {
        return set(uuid, get(uuid) - lives);
    }

    public static ApiResult death(Player player, String deathMessage) {
        UUID uuid = player.getUniqueId();
        int newLives = get(uuid) - 1;
        String banReason = null;
        Player killer = player.getKiller();
        if (killer == null) {
            player.getWorld().dropItem(player.getLocation(), LifeItem.LIFE.get(1));
        } else {
            if (get(killer.getUniqueId()) == Config.maxLives) {
                LifeItem.LIFE.giveTo(killer, 1);
            } else {
                add(killer.getUniqueId(), 1);
            }
        }
        FiveLives.getPlugin().getServer().broadcastMessage(deathMessage);
        if (newLives == 0) {
            if (killer == null) {
                banReason = translation("ban.reason.death").set("death-msg", deathMessage).get();
            } else {
                banReason = translation("ban.reason.killed").set("death-msg", deathMessage).get();
            }
        }
        return set(uuid, newLives, banReason);
    }

    public static ApiResult updateEffects(Player player) {
        int lives = get(player.getUniqueId());
        if (lives > Config.maxLives) {
            return ApiResult.MORE_THAN_MAX_LIVES;
        } else if (lives < 0) {
            return ApiResult.LESS_THAN_0_LIVES;
        }
        addEffects(player, lives);
        return ApiResult.SUCCESS;
    }

    public static ApiResult set(UUID uuid, int lives) {
        return set(uuid, lives, translationAsString("ban.reason.setTo0"));
    }

    private static ApiResult set(UUID uuid, int lives, String banReason) {
        if (lives > Config.maxLives) {
            return ApiResult.MORE_THAN_MAX_LIVES;
        } else if (lives < 0) {
            return ApiResult.LESS_THAN_0_LIVES;
        }
        Player player = Bukkit.getPlayer(uuid);
        Integer oldLives = LivesConfig.update(uuid, lives, lives == 0? new NoLifeInfo(banReason, System.currentTimeMillis()) : null);
        if (oldLives == null) {
            oldLives = Config.maxLives;
        }
        if (player != null) {
            removeEffects(player, oldLives);
            addEffects(player, lives);
            updateTab(player);
        }
        Log.info(Bukkit.getOfflinePlayer(uuid).getName() + "'s lives: " + oldLives + " -> " + lives);
        LivesConfig.getInstance().asyncSave();
        if (lives == 0) {
            if (player != null) {
                player.kickPlayer(BannedPreLoginChecker.banMessage(uuid));
            }
        }
        return ApiResult.SUCCESS;
    }

    public static ApiResult revive(UUID uuid) {
        if (get(uuid) != 0) {
            return ApiResult.PLAYER_NOT_ALLOWED;
        }
        return set(uuid, Config.reviveLives);
    }

    public static void fix(UUID uuid) {
        int lives = get(uuid);
        if (lives > Config.maxLives) {
            set(uuid, Config.maxLives);
        } else if (lives < 0) {
            set(uuid, 0);
        }
    }

    private static void removeEffects(Player player, int lives) {
        switch (lives) {
            case 1 -> {
                player.removePotionEffect(PotionEffectType.HUNGER);
                player.removePotionEffect(PotionEffectType.WEAKNESS);
            }
            case 2 -> player.removePotionEffect(PotionEffectType.HUNGER);
            case 4, 5 -> player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        }
    }

    private static void addEffects(Player player, int lives) {
        switch (lives) {
            case 1 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, PotionEffect.INFINITE_DURATION, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 0));
            }
            case 2 -> player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, PotionEffect.INFINITE_DURATION, 0));
            case 4 -> player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, PotionEffect.INFINITE_DURATION, 0));
            case 5 -> player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, PotionEffect.INFINITE_DURATION, 1));
        }
    }

    public static String getPrefix(UUID uuid) {
        return translation("prefix.format")
                .set(
                        "lives",
                        translationAsString("prefix.life")
                                .repeat(
                                        get(uuid)
                                )
                )
                .get();
    }

    public static void updateTab(Player player) {
        String lifeName = getPrefix(player.getUniqueId()) + player.getName();
        player.setPlayerListName(lifeName);
    }

    public static void handleApi(Supplier<ApiResult> call, Player player) {
        handleApi(call, player, true);
    }

    private static void handleApi(Supplier<ApiResult> call, Player player, boolean firstAttempt) {
        ApiResult result = call.get();
        if (result == ApiResult.SUCCESS) {
            return;
        }
        if (firstAttempt) {
            LifeApi.fix(player.getUniqueId());
            handleApi(call, player, false);
        } else {
            Log.err("Couldn't handle player respawn: " + result.name());
        }
    }
}

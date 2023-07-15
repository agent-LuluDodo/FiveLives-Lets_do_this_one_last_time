package de.luludodo.fivelives.api;

import de.luludodo.fivelives.config.LifeConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.UUID;

public class LifeApi {
    private static final LifeConfig config = LifeConfig.getInstance();

    public static int get(UUID uuid) {
        return config.uuidToLives.getOrDefault(uuid, config.maxLives);
    }

    public static ApiResult add(UUID uuid, int lives) {
        return set(uuid, get(uuid) + lives);
    }

    public static ApiResult remove(UUID uuid, int lives) {
        return set(uuid, get(uuid) - lives);
    }

    public static ApiResult set(UUID uuid, int lives) {
        if (lives > config.maxLives) {
            return ApiResult.MORE_THAN_MAX_LIVES;
        } else if (lives < 0) {
            return ApiResult.LESS_THAN_0_LIVES;
        }
        Player player = Bukkit.getPlayer(uuid);
        Integer oldLives = config.uuidToLives.put(uuid, lives);
        if (oldLives == null) {
            oldLives = config.maxLives;
        }
        if (lives == 0) {
            // Ban UUID
        }
        if (player != null) {
            removeEffects(player, oldLives);
            addEffects(player, lives);
            updateTab(player, lives);
        }
        return ApiResult.SUCCESS;
    }

    public static ApiResult update(UUID uuid) {
        int lives = get(uuid);
        if (lives > config.maxLives) {
            return ApiResult.MORE_THAN_MAX_LIVES;
        } else if (lives < 0) {
            return ApiResult.LESS_THAN_0_LIVES;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            updateTab(player, lives);
            return ApiResult.SUCCESS;
        }
        return ApiResult.PLAYER_UNKNOWN;
    }

    public static void fix(UUID uuid) {
        int lives = get(uuid);
        if (lives > config.maxLives) {
            set(uuid, config.maxLives);
        } else if (lives < 0) {
            set(uuid, 0);
        }
    }

    private static void removeEffects(Player player, int amount) {
        Collection<PotionEffect> potionEffects = player.getActivePotionEffects();
        switch (amount) {
            case 1 -> potionEffects.removeIf(potionEffect -> potionEffect.isInfinite() && (potionEffect.getType() == PotionEffectType.HUNGER || potionEffect.getType() == PotionEffectType.WEAKNESS) && potionEffect.getAmplifier() == 1);
            case 2 -> potionEffects.removeIf(potionEffect -> potionEffect.isInfinite() && potionEffect.getType() == PotionEffectType.HUNGER && potionEffect.getAmplifier() == 1);
            case 4 -> potionEffects.removeIf(potionEffect -> potionEffect.isInfinite() && potionEffect.getType() == PotionEffectType.FAST_DIGGING && potionEffect.getAmplifier() == 1);
            case 5 -> potionEffects.removeIf(potionEffect -> potionEffect.isInfinite() && potionEffect.getType() == PotionEffectType.FAST_DIGGING && potionEffect.getAmplifier() == 2);
        }
    }

    private static void addEffects(Player player, int amount) {
        Collection<PotionEffect> potionEffects = player.getActivePotionEffects();
        switch (amount) {
            case 1 -> {
                potionEffects.add(new PotionEffect(PotionEffectType.HUNGER, PotionEffect.INFINITE_DURATION, 1));
                potionEffects.add(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 1));
            }
            case 2 -> potionEffects.add(new PotionEffect(PotionEffectType.HUNGER, PotionEffect.INFINITE_DURATION, 1));
            case 4 -> potionEffects.add(new PotionEffect(PotionEffectType.FAST_DIGGING, PotionEffect.INFINITE_DURATION, 1));
            case 5 -> potionEffects.add(new PotionEffect(PotionEffectType.FAST_DIGGING, PotionEffect.INFINITE_DURATION, 2));
        }
    }
    private static void updateTab(Player player, int lives) {
        StringBuilder nameBuilder = new StringBuilder(config.tabPrefix);
        nameBuilder.append(String.valueOf(config.tabLife).repeat(lives));
        nameBuilder.append(config.tabSuffix);
        player.setPlayerListHeader(ChatColor.translateAlternateColorCodes('&', nameBuilder.toString()));
    }
}

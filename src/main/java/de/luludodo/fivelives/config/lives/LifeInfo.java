package de.luludodo.fivelives.config.lives;

import de.luludodo.fivelives.features.ban.NoLifeInfo;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LifeInfo {
    public static class LifeInfoException extends RuntimeException {
        public LifeInfoException(String msg) {
            super(msg);
        }
    }
    private int lives;
    private @Nullable NoLifeInfo noLifeInfo;
    public LifeInfo(int lives, @Nullable NoLifeInfo noLifeInfo) {
        if (lives == 0 && noLifeInfo == null) {
            throw new LifeInfoException("Missing NoLifeInfo");
        } else if (lives > 0 && noLifeInfo != null) {
            throw new LifeInfoException("Unnecessary NoLifeInfo");
        }
        this.lives = lives;
        this.noLifeInfo = noLifeInfo;
    }

    public void save(@NotNull ConfigurationSection section) {
        section.set("lives", lives);
        if (noLifeInfo != null) {
            ConfigurationSection noLifeInfoSection = section.createSection("noLifeInfo");
            noLifeInfoSection.set("reason", noLifeInfo.reason());
            noLifeInfoSection.set("date", noLifeInfo.date());
        }
    }

    public int getLives() {
        return lives;
    }

    public @Nullable NoLifeInfo getNoLifeInfo() {
        return noLifeInfo;
    }

    public int update(int lives, @Nullable NoLifeInfo noLifeInfo) {
        if (lives == 0 && noLifeInfo == null) {
            throw new LifeInfoException("Missing NoLifeInfo");
        } else if (lives > 0 && noLifeInfo != null) {
            throw new LifeInfoException("Unnecessary NoLifeInfo");
        }
        int oldLives = this.lives;
        this.lives = lives;
        this.noLifeInfo = noLifeInfo;
        return oldLives;
    }
}

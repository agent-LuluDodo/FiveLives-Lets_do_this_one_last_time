package de.luludodo.fivelives.config;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.UUID;

public class LifeConfig {
    private static LifeConfig instance;

    LifeConfig() {
        uuidToLives = new HashMap<>();
        tabPrefix = "&7(&c";
        tabSuffix = "&7)";
        tabLife = "❤";
        maxLives = 5;
        noLifeMessage = "&4&nDu hast keine Leben mehr übrig!\n" +
                        "\n" +
                        "&r&7Grund:&r %reason%\n" +
                        "&r&7Datum:&r %date%\n" +
                        "\n" +
                        "&r&cUm wieder joinen zu können musst\n" +
                        "du wiederbelebt werden!\n" +
                        "&7Nutze einen Beacon der Wiederbelebung\n" +
                        "um einen Spieler wiederzubeleben.";
    }

    public static @NonNull LifeConfig getInstance() {
        if (instance == null) {
            instance = new LifeConfig();
        }
        return instance;
    }

    public final HashMap<UUID, @NonNull Integer> uuidToLives;

    public final String tabPrefix;
    public final String tabSuffix;
    public final String tabLife;

    public final int maxLives;

    public final String noLifeMessage;
}

package de.luludodo.fivelives.config.translations;

import de.luludodo.fivelives.config.LuluConfig;
import de.luludodo.fivelives.features.item.LifeItem;
import de.luludodo.fivelives.log.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Translations extends LuluConfig {

    private static Translations instance;
    public static Translations getInstance() {
        if (instance == null) {
            instance = new Translations();
        }
        return instance;
    }

    private ConfigurationSection allTranslations;
    private String language;
    private ConfigurationSection translations;

    private Translations() {
        super("translations");
    }

    @Override
    protected void afterLoad() {
        allTranslations = notNull(config::getConfigurationSection, "translations");
        language = notNull(config::getString, "language");
        if (!setLanguage(language)) {
            Log.warn("Invalid language resetting to 'de'!");
            setLanguage("de");
        }
    }

    @Override
    protected void preSave() {
        config.set("language", language);
    }

    public boolean setLanguage(@NotNull String lang) {
        ConfigurationSection translations = allTranslations.getConfigurationSection(lang);
        if (allTranslations == null) {
            return false;
        }
        this.translations = translations;
        this.language = lang;
        LifeItem.loadAllTranslations();
        return true;
    }

    public Set<String> getLanguages() {
        return allTranslations.getKeys(false);
    }

    public @NotNull Translation getTranslation(@NotNull String key) {
        return new Translation(key, translations);
    }
}

package de.luludodo.fivelives.config;

import de.luludodo.fivelives.features.item.recipe.LifeRecipe;

public class Config extends LuluConfig {

    private static Config instance;
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Config() {
        super("config");
    }

    @Override
    protected void afterLoad() {
        maxLives = getInt("maxLives");
        reviveLives = getInt("reviveLives");
        fragmentRecipe = LifeRecipe.getOrCreateInstance("fragment");
        lifeRecipe = LifeRecipe.getOrCreateInstance("life");
        beaconRecipe = LifeRecipe.getOrCreateInstance("beacon");
        fragmentRecipe.load(notNull(config::getConfigurationSection, "fragmentRecipe"));
        lifeRecipe.load(notNull(config::getConfigurationSection, "lifeRecipe"));
        beaconRecipe.load(notNull(config::getConfigurationSection, "beaconRecipe"));
    }

    @Override
    protected void preSave() {
        config.set("maxLives", maxLives);
        config.set("reviveLives", reviveLives);
        fragmentRecipe.save(notNull(config::createSection, "beaconRecipe"));
        lifeRecipe.save(notNull(config::createSection, "lifeRecipe"));
        beaconRecipe.save(notNull(config::createSection, "beaconRecipe"));
    }

    public static int maxLives;
    public static int reviveLives;
    public static LifeRecipe fragmentRecipe;
    public static LifeRecipe lifeRecipe;
    public static LifeRecipe beaconRecipe;
}

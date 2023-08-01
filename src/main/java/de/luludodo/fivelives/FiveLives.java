package de.luludodo.fivelives;

import de.luludodo.fivelives.cmd.LifeCmd;
import de.luludodo.fivelives.cmd.RecipeCmd;
import de.luludodo.fivelives.cmd.WithdrawCmd;
import de.luludodo.fivelives.config.Config;
import de.luludodo.fivelives.config.lives.LivesConfig;
import de.luludodo.fivelives.config.translations.Translations;
import de.luludodo.fivelives.features.ban.BanLeaveMessageAndClear;
import de.luludodo.fivelives.features.ban.BannedPreLoginChecker;
import de.luludodo.fivelives.features.chat.ChatFormatChanger;
import de.luludodo.fivelives.features.death.PlayerDeathListener;
import de.luludodo.fivelives.features.effects.MilkOrHoneyConsumeListener;
import de.luludodo.fivelives.features.effects.PlayerRespawnListener;
import de.luludodo.fivelives.features.gui.GUIManager;
import de.luludodo.fivelives.features.item.recipe.DisableCustomItemsInNormalRecipes;
import de.luludodo.fivelives.features.join.NewPlayerRegisterer;
import de.luludodo.fivelives.features.revive.UseReviveListener;
import de.luludodo.fivelives.log.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FiveLives extends JavaPlugin {
    private static final LivesConfig livesConfig = LivesConfig.getInstance();
    private static final Translations translations = Translations.getInstance();
    private static final Config config = Config.getInstance();
    private static FiveLives plugin;

    public static FiveLives getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        Log.setLogger(getLogger());

        livesConfig.load();
        config.load();
        translations.load();

        getCommand("life").setExecutor(new LifeCmd());
        getCommand("withdraw").setExecutor(new WithdrawCmd());
        getCommand("recipe").setExecutor(new RecipeCmd());

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new GUIManager(), this);
        manager.registerEvents(new BanLeaveMessageAndClear(), this);
        manager.registerEvents(new BannedPreLoginChecker(), this);
        manager.registerEvents(new ChatFormatChanger(), this);
        manager.registerEvents(new PlayerDeathListener(), this);
        manager.registerEvents(new MilkOrHoneyConsumeListener(), this);
        manager.registerEvents(new PlayerRespawnListener(), this);
        manager.registerEvents(new DisableCustomItemsInNormalRecipes(), this);
        manager.registerEvents(new NewPlayerRegisterer(), this);
        manager.registerEvents(new UseReviveListener(), this);

        Log.info("Enabled FiveLives v1.0");
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer(translations.getTranslation("reload").get()));
    }

    @Override
    public void onDisable() {
        GUIManager.onDisable();

        livesConfig.save();
        config.save();
        translations.save();

        Log.info("Disabled FiveLives 1.0");
    }
}

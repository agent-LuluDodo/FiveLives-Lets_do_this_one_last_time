package de.luludodo.fivelives;

import de.luludodo.fivelives.log.Log;
import org.bukkit.plugin.java.JavaPlugin;

public final class FiveLives extends JavaPlugin {

    @Override
    public void onEnable() {
        Log.setLogger(getLogger());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

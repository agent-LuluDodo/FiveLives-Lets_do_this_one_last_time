package de.luludodo.fivelives.config;

import de.luludodo.fivelives.FiveLives;
import de.luludodo.fivelives.log.Log;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class LuluConfig {
    private static final HashMap<String, LuluConfig> nameToConfig = new HashMap<>();

    public static @Nullable LuluConfig getInstance(String name) {
        return nameToConfig.get(name);
    }

    public static Set<String> getNames() {
        return nameToConfig.keySet();
    }

    public static void forEach(Consumer<LuluConfig> action) {
        nameToConfig.values().forEach(action);
    }

    protected static class ConfigException extends RuntimeException {
        public ConfigException(@NotNull String message) {
            super(message);
        }
    }

    private File configFile;
    protected YamlConfiguration config;
    protected final @NotNull String name;

    public LuluConfig(@NotNull String name) {
        this.name = name + ".yml";
        nameToConfig.put(name, this);
    }

    public boolean load() {
        if (configFile == null) {
            configFile = new File(FiveLives.getPlugin().getDataFolder(), this.name);
        }
        if (!configFile.exists()) {
            FiveLives.getPlugin().saveResource(name, false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        try {
            afterLoad();
            Log.info("Loaded " + name);
            return true;
        } catch (ConfigException e) {
            Log.err("Couldn't load " + name + ": " + e.getMessage());
            return false;
        }
    }

    public void reset() {
        FiveLives.getPlugin().saveResource(name, true);
    }

    abstract protected void afterLoad();

    protected <O> @NotNull O notNull(Function<String, @Nullable O> func, @NotNull String path) {
        O o = func.apply(path);
        if (o == null) {
            throw new ConfigException("Field '" + path + "' missing for " + name);
        }
        return o;
    }

    protected int getInt(String path) {
        Integer o = (Integer) config.get(path, null);
        if (o == null) {
            throw new ConfigException("Field '" + path + "' missing for " + name);
        }
        return o;
    }

    abstract protected void preSave();

    public boolean save() {
        preSave();
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
        }
        config.options().copyDefaults(true);
        try {
            config.save(configFile);
            Log.info("Saved " + name);
            return true;
        } catch (IOException e) {
            Log.err("Couldn't save " + name + ": " + e.getMessage());
            return false;
        }
    }

    public Thread asyncSave() {
        Thread saveThread = new Thread(this::save, "fivelives-save-" + name);
        saveThread.setPriority(Thread.MIN_PRIORITY);
        saveThread.start();
        return saveThread;
    }

    public @NonNull String getName() {
        return name;
    }
}

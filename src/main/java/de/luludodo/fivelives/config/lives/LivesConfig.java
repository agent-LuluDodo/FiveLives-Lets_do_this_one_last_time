package de.luludodo.fivelives.config.lives;

import de.luludodo.fivelives.config.LuluConfig;
import de.luludodo.fivelives.features.ban.NoLifeInfo;
import de.luludodo.fivelives.log.Log;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class LivesConfig extends LuluConfig {

    private static LivesConfig instance;
    public static LivesConfig getInstance() {
        if (instance == null) {
            instance = new LivesConfig();
        }
        return instance;
    }

    private LivesConfig() {
        super("lives");
    }

    @Override
    protected void afterLoad() {
        Set<String> keys = config.getKeys(false);
        for (String key:keys) {
            try {
                uuidToLifeInfo.put(UUID.fromString(key), new LifeInfo(notNull(path -> (Integer) config.get(path), key + ".lives"), NoLifeInfo.get(config.getConfigurationSection(key + ".noLifeInfo"))));
            } catch (LifeInfo.LifeInfoException e) {
                Log.err("Invalid lifeInfo for '" + key + "': " + e.getMessage());
            }
        }
    }

    @Override
    protected void preSave() {
        uuidToLifeInfo.forEach((uuid, lifeInfo) -> lifeInfo.save(config.createSection(uuid.toString())));
    }

    public static final HashMap<UUID, LifeInfo> uuidToLifeInfo = new HashMap<>();

    public static NoLifeInfo getNoLifeInfo(UUID uuid) {
        return uuidToLifeInfo.get(uuid).getNoLifeInfo();
    }

    public static Integer update(UUID uuid, int lives, NoLifeInfo noLifeInfo) {
        LifeInfo lifeInfo = uuidToLifeInfo.get(uuid);
        if (lifeInfo == null) {
            uuidToLifeInfo.put(uuid, new LifeInfo(lives, noLifeInfo));
            return null;
        }
        return lifeInfo.update(lives, noLifeInfo);
    }
}

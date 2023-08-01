package de.luludodo.fivelives.features.ban;

import de.luludodo.fivelives.api.LifeApi;
import de.luludodo.fivelives.config.lives.LivesConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static de.luludodo.fivelives.config.translations.Translation.*;

public class BannedPreLoginChecker implements Listener {

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (LifeApi.get(event.getUniqueId()) == 0) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banMessage(event.getUniqueId()));
        }
    }

    public static @NotNull String banMessage(@NotNull UUID uuid) {
        NoLifeInfo noLifeInfo = LivesConfig.getNoLifeInfo(uuid);
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(noLifeInfo.date()), ZoneId.systemDefault());
        return translation("ban.kick")
                .set("reason", noLifeInfo.reason())
                .set(
                        "date",
                        translation("date")
                                .set("year", intToFixedString(date.getYear(), 4))
                                .set("month", intToFixedString(date.getMonthValue(), 2))
                                .set("day", intToFixedString(date.getDayOfMonth(), 2))
                                .set("hour", intToFixedString(date.getHour(), 2))
                                .set("minute", intToFixedString(date.getMinute(), 2))
                                .set("second", intToFixedString(date.getSecond(), 2))
                                .set("millisecond", intToFixedString(Math.round(date.getNano() / 1000000f), 3))
                                .get()
                )
                .get();
    }

    private static String intToFixedString(int i, int length) {
        String iString = String.valueOf(i);
        return new String(new char[length - iString.length()]).replace('\0', '0') + iString;
    }
}

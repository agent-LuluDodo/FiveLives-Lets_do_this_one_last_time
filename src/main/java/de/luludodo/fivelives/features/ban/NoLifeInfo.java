package de.luludodo.fivelives.features.ban;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public class NoLifeInfo {
    private final String reason;
    private final long date;

    public NoLifeInfo(String reason, long date) {
        this.reason = reason;
        this.date = date;
    }

    public String reason() {
        return reason;
    }

    public long date() {
        return date;
    }

    public static class NoLifeInfoFormatException extends RuntimeException {
        public NoLifeInfoFormatException(String msg) {
            super(msg);
        }
    }

    public static @Nullable NoLifeInfo get(@Nullable ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String reason = section.getString("reason");
        long date = section.getLong("date");
        if (reason == null) {
            throw new NoLifeInfoFormatException("Field 'reason' missing");
        }
        if (date == 0) {
            throw new NoLifeInfoFormatException("Field 'date' missing");
        }
        return new NoLifeInfo(reason, date);
    }
}
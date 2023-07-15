package de.luludodo.fivelives.log;

import java.util.logging.Logger;

public class Log {
    private static Logger log;

    public static void setLogger(Logger log) {
        Log.log = log;
    }

    public static void info(String msg) {
        log.info(msg);
    }

    public static void warn(String msg) {
        log.warning(msg);
    }

    public static void err(String msg) {
        log.severe(msg);
    }
}

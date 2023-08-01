package de.luludodo.fivelives.cmd;

import de.luludodo.fivelives.features.send.FeedbackSender;
import de.luludodo.fivelives.log.Log;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static de.luludodo.fivelives.config.translations.Translation.*;

public abstract class LuluTabExecutor implements TabExecutor {
    private static final String TOO_LITTLE_ARGS = "cmd.error.tooLittleArgs";
    private static final String TOO_MANY_ARGS = "cmd.error.tooManyArgs";

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        feedback = new FeedbackSender(sender);
        this.args = args;
        command(feedback, sender, command, label, args);
        return true;
    }

    abstract protected void command(@NonNull FeedbackSender feedback, @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args);

    protected Player requirePlayer(CommandSender sender, boolean silent) {
        if (sender instanceof Player) {
            return (Player) sender;
        } else if (!silent) {
            feedback.sendError(translationAsString("cmd.error.notAPlayer"));
        }
        return null;
    }
    private FeedbackSender feedback;
    private String[] args;

    protected @NotNull String getName(@Nullable UUID uuid) {
        if (uuid == null) {
            return "null";
        } else {
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) {
                return uuid.toString();
            } else {
                return name;
            }
        }
    }

    protected UUID getUUID(int arg) {
        String s = args[arg];
        UUID uuid;
        try {
            uuid = UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            uuid = Bukkit.getOfflinePlayer(s).getUniqueId();
        }
        return uuid;
    }

    // min & max -> inclusive
    protected Integer getInt(int arg, int min, int max) {
        int i;
        try {
            i = Integer.parseInt(args[arg]);
        } catch (NumberFormatException e) {
            feedback.sendError(arg, translationAsString("cmd.error.notANumber"));
            return null;
        }
        if (i > max) {
            feedback.sendError(arg, translationAsString("cmd.error.tooBigNumber"));
            return null;
        } else if (i < min) {
            feedback.sendError(arg, translationAsString("cmd.error.tooSmallNumber"));
            return null;
        }
        return i;
    }

    protected boolean hasArgs(int amount) {
        if (args.length > amount) {
            feedback.sendError(translationAsString(TOO_MANY_ARGS));
            return false;
        } else if (args.length < amount) {
            feedback.sendError(translationAsString(TOO_LITTLE_ARGS));
            return false;
        }
        return true;
    }

    protected boolean hasArgs(int min, int max) {
        if (args.length > max) {
            feedback.sendError(translationAsString(TOO_MANY_ARGS));
            return false;
        } else if (args.length < min) {
            feedback.sendError(translationAsString(TOO_LITTLE_ARGS));
            return false;
        }
        return true;
    }

    protected boolean hasMinArgs(int min) {
        if (args.length < min) {
            feedback.sendError(translationAsString(TOO_LITTLE_ARGS));
            return false;
        }
        return true;
    }

    protected List<String> tabComplete;
    private String arg;

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        tabComplete = new ArrayList<>();
        this.args = args;
        arg = args[args.length - 1];
        tabComplete(sender, command, label, args);
        return tabComplete;
    }

    abstract protected void tabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args);

    protected List<String> getAllPlayerNames() {
        return getAllPlayerNames(player -> true);
    }

    protected List<String> getAllPlayerNames(Function<OfflinePlayer, Boolean> condition) {
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        List<String> playerNames = new ArrayList<>(offlinePlayers.length);
        for (OfflinePlayer player:offlinePlayers) {
            if (condition.apply(player)) {
                playerNames.add(player.getName());
            }
        }
        return playerNames;
    }

    protected void add(String option) {
        if (option.startsWith(arg)) {
            tabComplete.add(option);
        }
    }

    protected void addAll(String... options) {
        for (String option:options) {
            add(option);
        }
    }

    protected void addAll(Collection<String> options) {
        for (String option:options) {
            add(option);
        }
    }

    // from & to -> inclusive
    protected void addInts(int from, int to) {
        if (to - from > 0) {
            List<String> ints = new ArrayList<>(to - from);
            for (int i = from; i <= to; i++) {
                String s = String.valueOf(i);
                if (s.startsWith(arg) && !s.equals(arg)) {
                    ints.add(s);
                }
            }
            if (ints.size() <= 10) {
                tabComplete.addAll(ints);
            } else {
                tabComplete.add(ints.get(0));
                tabComplete.add(ints.get(ints.size() - 1));
            }
        }
    }

    protected void addTitle(String title, boolean optional) {
        if (tabComplete.isEmpty()) {
            if (optional) {
                tabComplete.add("<[" + title + "]>");
            } else {
                tabComplete.add("<" + title + ">");
            }
        }
    }
}


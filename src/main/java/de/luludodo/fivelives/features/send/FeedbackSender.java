package de.luludodo.fivelives.features.send;

import de.luludodo.fivelives.api.ApiResult;
import de.luludodo.fivelives.log.Log;
import org.bukkit.command.CommandSender;

import java.util.Objects;

import static de.luludodo.fivelives.config.translations.Translation.*;

public class FeedbackSender {

    private final CommandSender sender;
    public FeedbackSender(CommandSender sender) {
        this.sender = sender;
    }

    public static void sendError(CommandSender sender, int arg, String message) {
        sender.sendMessage(
                translation("cmd.send.argError")
                        .set("arg", arg + 1)
                        .set("msg", message)
                        .get()
        );
    }
    public void sendError(int arg, String message) {
        sendError(sender, arg, message);
    }

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(
                translation("cmd.send.error")
                        .set("msg", message)
                        .get()
        );
    }
    public void sendError(String message) {
        sendError(sender, message);
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(
                translation("cmd.send.success")
                        .set("msg", message)
                        .get()
        );
    }
    public void sendSuccess(String message) {
        sendSuccess(sender, message);
    }

    public static void sendInternalError(CommandSender sender, String message) {
        sender.sendMessage(
                translation("cmd.send.internalError")
                        .set("msg", message)
                        .get()
        );
    }
    public void sendInternalError(String message) {
        sendInternalError(sender, message);
    }

    public static void sendApiResult(CommandSender sender, String successMessage, ApiResult... results) {
        int successes = 0;
        for (ApiResult result:results) {
            if (result == ApiResult.SUCCESS) {
                successes++;
            } else {
                sendInternalError(sender, result.getMessage());
            }
        }
        if (successes == results.length) {
            sendSuccess(sender, successMessage);
        } else {
            sendError(
                    sender,
                    translation("cmd.send.successOutOf")
                            .set("successes", successes)
                            .set("results", results.length)
                            .get()
            );
        }
    }
    public void sendApiResult(String successMessage, ApiResult... results) {
        sendApiResult(sender, successMessage, results);
    }
}

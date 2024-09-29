package com.racingprodigy.standings_bot.util;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class MessageUtil {

    public static void sendStackTraceToChannel(
            String message,
            TextChannel channel,
            Throwable error) {

        String stackTrace = ExceptionUtils.getStackTrace(error);

        channel.sendMessage(getJavaFormat(message)).queue();
        channel.sendMessage(getJavaFormat(stackTrace.substring(0, Math.min(stackTrace.length(), 1000)))).queue();
    }

    private static String getJavaFormat(String message) {
        return String.format("```java\n%s\n```", message);
    }
}

package com.racingprodigy.standings_bot.commands;

import com.racingprodigy.standings_bot.data.RPIracingDriver;
import com.racingprodigy.standings_bot.data.RPIracingDriverRepository;
import com.racingprodigy.standings_bot.util.CsvHelper;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;

import static com.racingprodigy.standings_bot.util.MessageUtil.sendStackTraceToChannel;

@Component
public class UploadDriverCsvCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadDriverCsvCommand.class);

    @Value("${racingprodigy.channel.error}")
    private String errorChannelId;

    @Autowired
    private RPIracingDriverRepository rpIracingDriverRepository;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("uploadlist")) {
            event.deferReply(true).queue();
            var attachment = event.getOption("csvfile").getAsAttachment();

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(attachment.getUrl())
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                var ids = CsvHelper.readUniqueIracingCustomerIDs(new InputStreamReader(response.body().byteStream()));
                LOGGER.info("Deleting previous driver entries");
                rpIracingDriverRepository.deleteAll();

                for (var id : ids) {
                    var entry = new RPIracingDriver(id);
                    rpIracingDriverRepository.save(entry);
                }
                LOGGER.info("Finished saving new driver entries");
            } catch (Exception e) {
                LOGGER.error("Failed to read CSV", e);
                var channel = event.getJDA().getChannelById(TextChannel.class, errorChannelId);
                sendStackTraceToChannel("Failed to read CSV", channel, e);
                event.getHook().sendMessage("Failed to read CSV").setEphemeral(true).queue();
                return;
            }

            var count = rpIracingDriverRepository.count();
            event.getHook().sendMessage(String.format("Found %d unique ids and saved for next refresh", count)).queue();
        }
    }
}
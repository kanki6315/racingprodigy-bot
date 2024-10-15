package com.racingprodigy.standings_bot.commands;

import com.racingprodigy.standings_bot.data.RPIracingDriverRepository;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

import static com.racingprodigy.standings_bot.util.MessageUtil.sendStackTraceToChannel;

@Component
public class CheckStandingsCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckStandingsCommand.class);

    @Value("${racingprodigy.channel.error}")
    private String errorChannelId;

    @Autowired
    private RPIracingDriverRepository rpIracingDriverRepository;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("checkstandings")) {
            event.deferReply(true).queue();

            try {
                var iRacingID = event.getOption("iracingid").getAsInt();
                var championship = event.getOption("championship").getAsString();

                // if (championship.equals("MX5") {
                var count = rpIracingDriverRepository.countRPIracingDriverByPositionIsNotNull();
                LOGGER.info("Counted {} drivers with position", count);
                DecimalFormat df = new DecimalFormat("##.##%");

                rpIracingDriverRepository.findById(Integer.toString(iRacingID))
                        .ifPresentOrElse((driver) -> {
                            if (driver.getPosition().isPresent()) {
                                var percentageChamp = (double) driver.getPosition().get() / count;
                                event.getHook().sendMessage("Position:  " + driver.getPosition().get() + ". Currently in Top " + df.format(percentageChamp)).setEphemeral(true).queue();

                            } else {
                                event.getHook().sendMessage("Driver is not currently placed in championship").setEphemeral(true).queue();
                            }
                        }, () -> event.getHook().sendMessage("Racing Prodigy Driver was not found with the iRacing ID: " + iRacingID).setEphemeral(true).queue());

            } catch (Exception e) {
                LOGGER.error("Failed to determine championship position", e);
                var channel = event.getJDA().getChannelById(TextChannel.class, errorChannelId);
                sendStackTraceToChannel("Failed to return championship position", channel, e);
                event.getHook().sendMessage("Failed to determine championship position, please try again later.").setEphemeral(true).queue();
            }
        }
    }
}

package com.racingprodigy.standings_bot.commands;

import com.racingprodigy.standings_bot.data.RPIracingDriver;
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

                RPIracingDriver.Series series;
                if (championship.equals("MX-5")) {
                    series = RPIracingDriver.Series.GLOBAL_MAZDA;
                } else if (championship.equals("GR86")) {
                    series = RPIracingDriver.Series.GR86;
                } else {
                    throw new RuntimeException("Unable to match championship to input" + championship);
                }

                var count = rpIracingDriverRepository.countRPIracingDriverBySeriesTypeEqualsAndPositionIsNotNull(series);
                LOGGER.info("Counted {} drivers with position", count);
                DecimalFormat df = new DecimalFormat("##.##%");

                rpIracingDriverRepository.findByIdAndSeriesTypeEquals(iRacingID, series)
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

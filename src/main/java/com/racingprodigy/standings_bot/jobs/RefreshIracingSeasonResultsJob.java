package com.racingprodigy.standings_bot.jobs;

import com.racingprodigy.standings_bot.data.RPIracingDriver;
import com.racingprodigy.standings_bot.data.RPIracingDriverRepository;
import com.racingprodigy.standings_bot.iracing.IracingService;
import com.racingprodigy.standings_bot.template.TableService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;

import static com.racingprodigy.standings_bot.util.MessageUtil.sendStackTraceToChannel;

@Component
public class RefreshIracingSeasonResultsJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshIracingSeasonResultsJob.class);

    private final static String seasonID = "5029";
    private final static String carClassID = "74";

    @Autowired
    private JDA api;
    @Autowired
    private IracingService iracingService;
    @Autowired
    private TableService tableService;
    @Autowired
    private RPIracingDriverRepository rpIracingDriverRepository;

    @Value("${racingprodigy.channel.error}")
    private String errorChannelId;

    @Value("${racingprodigy.channel.image_channel}")
    private String imageChannelId;

    @Scheduled(fixedRate = 30 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    public void run() {
        try {
            var rpDrivers = rpIracingDriverRepository.findAll();
            var ids = rpDrivers.stream().map(RPIracingDriver::getId).toList();

            if (ids.isEmpty()) {
                LOGGER.info("No IDs found, skipping iRacing pull");
                return;
            }

            LOGGER.info("Updating iRacing results");

            var seasonStandings = iracingService.getSeriesResults(seasonID, carClassID);
            var seasonChunk = iracingService.getChunkSeriesResult(seasonStandings.chunkInfo().baseDownloadUrl() + seasonStandings.chunkInfo().chunkFileNames().get(0));
            var filteredChunk = seasonChunk.stream().filter(d -> ids.contains(d.custId())).toList();

            var seasonInfo = iracingService.getiRacingSeries(seasonID);
            var raceWeek = seasonInfo.schedules().stream().filter((w) -> w.raceWeekNum() == seasonInfo.raceWeek()).findFirst().get();
            var weekNumber = seasonInfo.raceWeek() + 1;
            var trackName = raceWeek.track().trackName();

            LOGGER.info("Generating image");

            var table = tableService.getImageTable(filteredChunk, weekNumber, trackName);

            LOGGER.info("Checking for previous message");

            var channel = api.getTextChannelById(imageChannelId);
            var message = channel.getIterableHistory().stream().filter((m) -> m.getAuthor().equals(api.getSelfUser())).findFirst();

            if (message.isPresent()) {
                LOGGER.info("Found previous message");

                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                var midnightTuesday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0);
                var midnightPlus = midnightTuesday.plusMinutes(30);

                if (now.isAfter(midnightTuesday) && now.isBefore(midnightPlus) && message.get().getTimeCreated().isBefore(midnightTuesday)) {
                    LOGGER.info("Creating new posts in channel");
                    channel.sendMessage(MessageCreateData.fromContent(String.format("# Week %d - %s", weekNumber, trackName))).queue();
                    channel.sendMessage(MessageCreateData.fromFiles(table)).queue();
                } else {
                    LOGGER.info("Editing previous post");
                    message.get().editMessage(MessageEditData.fromFiles(table)).queue();
                }
            } else {
                LOGGER.info("Posting for first time in channel");
                channel.sendMessage(MessageCreateData.fromContent(String.format("# Week %d - %s", weekNumber, trackName))).queue();
                channel.sendMessage(MessageCreateData.fromFiles(table)).queue();
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to pull iRacing results", ex);
            var channel = api.getChannelById(TextChannel.class, errorChannelId);
            sendStackTraceToChannel("Failed to pull iRacing results", channel, ex);
        }
    }
}

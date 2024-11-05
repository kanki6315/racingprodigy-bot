package com.racingprodigy.standings_bot.jobs;

import com.racingprodigy.standings_bot.data.RPIracingDriver;
import com.racingprodigy.standings_bot.data.RPIracingDriverRepository;
import com.racingprodigy.standings_bot.iracing.IracingService;
import com.racingprodigy.standings_bot.iracing.model.IracingSeasonStandingChunkResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.racingprodigy.standings_bot.util.MessageUtil.sendStackTraceToChannel;

@Component
public class PullMetricsIracingSeasonJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(PullMetricsIracingSeasonJob.class);

    @Autowired
    private JDA api;
    @Autowired
    private IracingService iracingService;
    @Autowired
    private RPIracingDriverRepository rpIracingDriverRepository;

    @Value("${racingprodigy.channel.error}")
    private String errorChannelId;

    @Value("${racingprodigy.channel.reporting_channel}")
    private String reportingChannelId;

    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        try {
            var rpDrivers = rpIracingDriverRepository.findAllBySeriesTypeEquals(RPIracingDriver.Series.GLOBAL_MAZDA);
            var ids = rpDrivers.stream().map(RPIracingDriver::getId).toList();

            if (ids.isEmpty()) {
                LOGGER.info("No IDs found, skipping iRacing pull");
                return;
            }

            LOGGER.info("Pulling iRacing data");

            pullMetricsForSeries(ids, RPIracingDriver.Series.GLOBAL_MAZDA);
            pullMetricsForSeries(ids, RPIracingDriver.Series.GR86);
        } catch (Exception ex) {
            LOGGER.error("Failed to pull iRacing results", ex);
            var channel = api.getChannelById(TextChannel.class, errorChannelId);
            sendStackTraceToChannel("Failed to pull iRacing results", channel, ex);
        }
    }

    private void pullMetricsForSeries(List<Integer> ids, RPIracingDriver.Series series) throws Exception {
        var seasonStandings = iracingService.getSeriesResults(series.getSeasonID(), series.getCarClassID());
        List<IracingSeasonStandingChunkResponse> seasonChunk = new ArrayList<>();
        for (String s : seasonStandings.chunkInfo().chunkFileNames()) {
            seasonChunk.addAll(iracingService.getChunkSeriesResult(seasonStandings.chunkInfo().baseDownloadUrl() + s));
        }

        List<IracingSeasonStandingChunkResponse> filteredDrivers;
        if (series.isNeedsFiltering()) {
            filteredDrivers = seasonChunk.stream().filter(d -> ids.contains(d.custId())).toList();
        } else {
            filteredDrivers = seasonChunk;
        }

        var countUnique = filteredDrivers.size();
        var mostRacesDone = filteredDrivers.stream().mapToInt(IracingSeasonStandingChunkResponse::starts).max().orElse(0);
        var totalStarts = filteredDrivers.stream().mapToInt(IracingSeasonStandingChunkResponse::starts).sum();
        var avgStarts = totalStarts / (countUnique);

        var builder = new EmbedBuilder()
                .setTitle("Analytics " + series.name() + " - " + LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yy")))
                .addField("Unique Drivers", Integer.toString(countUnique), true)
                .addField("Most Races Done", Integer.toString(mostRacesDone), true)
                .addField("Avg Starts per Driver", Integer.toString(avgStarts), true)
                .addField("Total Starts", Integer.toString(totalStarts), true).build();

        var channel = api.getTextChannelById(reportingChannelId);
        channel.sendMessageEmbeds(builder).queue();
    }
}

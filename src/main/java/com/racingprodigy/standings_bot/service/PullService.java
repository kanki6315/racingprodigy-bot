package com.racingprodigy.standings_bot.service;

import com.racingprodigy.standings_bot.data.RPIracingDriver;
import com.racingprodigy.standings_bot.data.RPIracingDriverRepository;
import com.racingprodigy.standings_bot.iracing.IracingService;
import com.racingprodigy.standings_bot.iracing.model.IracingSeasonStandingChunkResponse;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PullService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PullService.class);

    @Autowired
    private IracingService iracingService;

    @Autowired
    private RPIracingDriverRepository rpIracingDriverRepository;

    @NotNull
    @Transactional
    public List<IracingSeasonStandingChunkResponse> getIracingSeasonStandings(
            RPIracingDriver.Series series) throws Exception {

        var rpDrivers = rpIracingDriverRepository.findAllBySeriesTypeEquals(series);

        if (rpDrivers.isEmpty() && series.isNeedsFiltering()) {
            LOGGER.error("No IDs found for series that needs filtering");
            throw new RuntimeException("No IDs found for series that needs filtering");
        }

        if (!series.isNeedsFiltering()) {
            rpIracingDriverRepository.deleteAllBySeriesTypeEquals(series);
        }

        var ids = rpDrivers.stream().map(RPIracingDriver::getId).toList();

        var seasonStandings = iracingService.getSeriesResults(series.getSeasonID(), series.getCarClassID());
        var filteredStandings = new ArrayList<IracingSeasonStandingChunkResponse>();

        for (var fileName : seasonStandings.chunkInfo().chunkFileNames()) {
            LOGGER.info("requesting page {}", fileName);
            var seasonChunk = iracingService.getChunkSeriesResult(seasonStandings.chunkInfo().baseDownloadUrl() + fileName);
            if (series.isNeedsFiltering()) {
                filteredStandings.addAll(seasonChunk.stream().filter(d -> ids.contains(d.custId())).toList());
            } else {
                filteredStandings.addAll(seasonChunk);
            }
        }

        LOGGER.info("Caching results");
        int currentPosition = 1;

        var updatedDrivers = new ArrayList<RPIracingDriver>();
        for (var i = 0; i < filteredStandings.size(); i++) {
            var filteredStanding = filteredStandings.get(i);
            RPIracingDriver rpDriver;
            if (series.isNeedsFiltering()) {
                rpDriver = rpDrivers.stream().filter(d -> d.getId().equals(filteredStanding.custId())).findFirst().get();
            } else {
                rpDriver = new RPIracingDriver(filteredStanding.custId(), series);
            }

            if (currentPosition > 1 && filteredStanding.points() == filteredStandings.get(i - 1).points()) {
                // If points are the same as the previous driver, assign the same position
                rpDriver.setPosition(updatedDrivers.get(i - 1).getPosition().get());
            } else {
                // Otherwise, assign the current position
                rpDriver.setPosition(currentPosition);
            }

            updatedDrivers.add(rpDriver);
            rpIracingDriverRepository.save(rpDriver);
            currentPosition++;
        }
        return filteredStandings;
    }
}

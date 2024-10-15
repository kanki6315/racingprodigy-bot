package com.racingprodigy.standings_bot.service;

import com.racingprodigy.standings_bot.data.RPIracingDriver;
import com.racingprodigy.standings_bot.data.RPIracingDriverRepository;
import com.racingprodigy.standings_bot.iracing.IracingService;
import com.racingprodigy.standings_bot.iracing.model.IracingSeasonStandingChunkResponse;
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
    public ArrayList<IracingSeasonStandingChunkResponse> getIracingSeasonStandings(
            List<RPIracingDriver> rpDrivers,
            String seasonID,
            String carClassID) throws Exception {
        var ids = rpDrivers.stream().map(RPIracingDriver::getId).toList();

        var seasonStandings = iracingService.getSeriesResults(seasonID, carClassID);
        var filteredStandings = new ArrayList<IracingSeasonStandingChunkResponse>();

        for (var fileName : seasonStandings.chunkInfo().chunkFileNames()) {
            LOGGER.info("requesting page {}", fileName);
            var seasonChunk = iracingService.getChunkSeriesResult(seasonStandings.chunkInfo().baseDownloadUrl() + fileName);
            filteredStandings.addAll(seasonChunk.stream().filter(d -> ids.contains(d.custId())).toList());
        }

        LOGGER.info("Caching results");
        int currentPosition = 1;

        var updatedDrivers = new ArrayList<RPIracingDriver>();
        for (var i = 0; i < filteredStandings.size(); i++) {
            var filteredStanding = filteredStandings.get(i);
            var rpDriver = rpDrivers.stream().filter(d -> d.getId().equals(filteredStanding.custId())).findFirst().get();

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

package com.racingprodigy.standings_bot.iracing.model;

import java.time.OffsetDateTime;
import java.util.List;

public record IracingSeasonStandingsResponse(boolean success,
                                             int seasonId,
                                             String seasonName,
                                             String seasonShortName,
                                             int seriesId,
                                             String seriesName,
                                             int carClassId,
                                             int raceWeekNum,
                                             int division,
                                             int clubId,
                                             int customerRank,
                                             ChunkInfo chunkInfo,
                                             OffsetDateTime lastUpdated,
                                             String csvUrl) {

    public record ChunkInfo(int chunkSized,
                            int numChunks,
                            int rows,
                            String baseDownloadUrl,
                            List<String> chunkFileNames) {
    }
}

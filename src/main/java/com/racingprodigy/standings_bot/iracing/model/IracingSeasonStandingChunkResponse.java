package com.racingprodigy.standings_bot.iracing.model;

public record IracingSeasonStandingChunkResponse(int rank,
                                                 int custId,
                                                 String displayName,
                                                 int division,
                                                 String countryCode,
                                                 String country,
                                                 int clubId,
                                                 String clubName,
                                                 LicenseInfo license,
                                                 IracingDriverResponse.HelmetDetails helmet,
                                                 int weeksCounted,
                                                 int starts,
                                                 int wins,
                                                 int top5,
                                                 int top25Percent,
                                                 int poles,
                                                 int avgStartPosition,
                                                 int avgFinishPosition,
                                                 int avgFieldSize,
                                                 int laps,
                                                 int lapsLed,
                                                 int incidents,
                                                 int points,
                                                 double rawPoints,
                                                 boolean weekDropped) {

    public record LicenseInfo(int categoryId,
                              String category,
                              int licenseLevel,
                              double safetyRating,
                              int irating,
                              String color,
                              String groupName,
                              int groupId) {
    }
}

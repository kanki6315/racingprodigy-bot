package com.racingprodigy.standings_bot.iracing.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record IracingSeasonDetailsResponse(int seasonId,
                                           String seasonName,
                                           boolean active,
                                           AllowedSeasonMembers allowedSeasonMembers,
                                           List<Integer> carClassIds,
                                           List<CarTypes> carTypes,
                                           boolean cautionLapsDoNotCount,
                                           boolean complete,
                                           boolean crossLicense,
                                           boolean distributedMatchmaking,
                                           int driverChangeRule,
                                           boolean driverChanges,
                                           int drops,
                                           boolean enablePitlaneCollisions,
                                           boolean fixedSetup,
                                           int greenWhiteCheckeredLimit,
                                           boolean gridByClass,
                                           int hardcoreLevel,
                                           boolean hasSupersessions,
                                           boolean ignoreLicenseForPractice,
                                           int incidentLimit,
                                           int incidentWarnMode,
                                           int incidentWarnParam1,
                                           int incidentWarnParam2,
                                           boolean isHeatRacing,
                                           int licenseGroup,
                                           List<LicenseGroupTypes> licenseGroupTypes,
                                           boolean luckyDog,
                                           int maxTeamDrivers,
                                           int maxWeeks,
                                           int minTeamDrivers,
                                           boolean multiclass,
                                           boolean mustUseDiffTireTypesInRace,
                                           NextRaceSession nextRaceSession,
                                           int numFastTows,
                                           int numOptLaps,
                                           boolean official,
                                           int opDuration,
                                           int openPracticeSessionTypeId,
                                           boolean qualifierMustStartRace,
                                           int raceWeek,
                                           int raceWeekToMakeDivisions,
                                           int regUserCount,
                                           boolean regionCompetition,
                                           boolean restrictByMember,
                                           boolean restrictToCar,
                                           boolean restrictViewing,
                                           String rookieSeason,
                                           String scheduleDescription,
                                           List<Schedules> schedules,
                                           int seasonQuarter,
                                           String seasonShortName,
                                           int seasonHear,
                                           boolean sendToOpenPractice,
                                           int seriesId,
                                           boolean shortParadeLap,
                                           OffsetDateTime startDate,
                                           boolean startOnQualTire,
                                           boolean startZone,
                                           List<TrackTypes> trackTypes,
                                           int unsportConductRuleMode) {

    public record AllowedSeasonMembers(int allowedSeasonMembers) {

    }

    public record CarTypes(String carType) {
    }

    public record LicenseGroupTypes(int licenseGroupType) {
    }

    public record NextRaceSession() {
    }

    public record TrackTypes(String trackType) {
    }

    public record Schedules(int seasonId,
                            int raceWeekNum,
                            List<CarRestrictions> carRestrictions,
                            String category,
                            int categoryId,
                            boolean enablePitlaneCollisions,
                            boolean fullCourseCautions,
                            int practiceLength,
                            boolean qualAttached,
                            int qualifyLaps,
                            int qualifyLength,
                            Integer raceLapLimit,
                            List<RaceTimeDescriptors> raceTimeDescriptors,
                            Integer raceTimeLimit,
                            List<RaceWeekCars> raceWeekCars,
                            String restartType,
                            String scheduleName,
                            String seasonName,
                            int seriesId,
                            String seriesName,
                            boolean shortParadeLap,
                            String specialEventType,
                            LocalDate startDate,
                            String startType,
                            boolean startZone,
                            Track track,
                            TrackState trackState,
                            int warmupLength,
                            Weather weather,
                            String weatherUrl,
                            int windDir,
                            int windUnits,
                            int windValue) {

        public record CarRestrictions(int carId,
                                      int maxDryTireSets,
                                      float maxPctFuelFill,
                                      float powerAdjustPct,
                                      int raceSetupId,
                                      float weightPenaltyKg) {
        }

        public record RaceTimeDescriptors(List<Integer> dayOffset,
                                          String firstSessionTime,
                                          int repeatMinutes,
                                          boolean repeating,
                                          int sessionMinutes,
                                          LocalDate startDate,
                                          boolean superSession) {
        }

        public record RaceWeekCars() {
        }

        public record Track(String category,
                            int categoryId,
                            int trackId,
                            String trackName) {
        }

        public record TrackState(boolean leaveMarbles) {
        }

        public record Weather(boolean allowFog,
                              ForecastOptions forecastOptions,
                              int precipOption,
                              int relHumidity,
                              String simulatedStartTime,
                              int simulatedTimeMultiplier,
                              List<Integer> simulatedTimeOffsets,
                              int skies,
                              int tempUnits,
                              int tempValue,
                              int timeOfDay,
                              int trackWater,
                              int version,
                              WeatherSummary weatherSummary) {

            public record ForecastOptions(int forecastType,
                                          int precipitation,
                                          int skies,
                                          int stopPrecip,
                                          int temperature,
                                          long weatherSeed,
                                          int windDir,
                                          int windSpeed) {
            }

            public record WeatherSummary(float maxPrecipRate,
                                         String maxPrecipRateDesc,
                                         float precipChance,
                                         int skiesHigh,
                                         int skiesLow,
                                         float tempHigh,
                                         float tempLow,
                                         int tempUnits,
                                         float windHigh,
                                         float windLow,
                                         int windUnits) {
            }
        }
    }
}

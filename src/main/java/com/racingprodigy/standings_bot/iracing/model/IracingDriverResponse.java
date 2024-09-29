package com.racingprodigy.standings_bot.iracing.model;

public record IracingDriverResponse(int custId,
                                    String displayName,
                                    HelmetDetails helmet,
                                    boolean profileDisabled) {

    public record HelmetDetails(int pattern,
                                String color1,
                                String color2,
                                String color3,
                                int faceType,
                                int helmetType) {
    }
}

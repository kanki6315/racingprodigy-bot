package com.racingprodigy.standings_bot.iracing.model;

import java.time.OffsetDateTime;

public record IracingLinkResponse(String link, OffsetDateTime expires) {
}

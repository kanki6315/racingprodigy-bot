package com.racingprodigy.standings_bot.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RPIracingDriverRepository extends JpaRepository<RPIracingDriver, String> {
}
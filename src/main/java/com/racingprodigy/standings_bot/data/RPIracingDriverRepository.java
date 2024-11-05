package com.racingprodigy.standings_bot.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RPIracingDriverRepository extends JpaRepository<RPIracingDriver, String> {
    Integer countRPIracingDriverBySeriesTypeEqualsAndPositionIsNotNull(RPIracingDriver.Series seriesType);

    List<RPIracingDriver> findAllBySeriesTypeEquals(RPIracingDriver.Series seriesType);

    void deleteAllBySeriesTypeEquals(RPIracingDriver.Series seriesType);

    Optional<RPIracingDriver> findByIdAndSeriesTypeEquals(Integer id, RPIracingDriver.Series seriesType);
}
package com.racingprodigy.standings_bot.data;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "iracing_driver")
@IdClass(RPIracingDriver.RPIracingDriverId.class)
public class RPIracingDriver {

    @Id
    private Integer id;

    @Id
    private Series seriesType;

    @Nullable
    private Integer position;

    public RPIracingDriver() {
    }

    public RPIracingDriver(Integer id, Series seriesType) {
        this.id = id;
        this.seriesType = seriesType;
    }

    public Integer getId() {
        return id;
    }

    public Series getSeriesType() {
        return seriesType;
    }

    public Optional<Integer> getPosition() {
        return Optional.ofNullable(position);
    }

    public void setPosition(@Nullable Integer position) {
        this.position = position;
    }

    public enum Series {
        GLOBAL_MAZDA("5029", "74", true, "mx5ImageBackground"),
        GR86("5195", "4012", false, "gr86ImageBackground");

        private String seasonID;
        private String carClassID;
        private boolean needsFiltering;
        private String fileName;

        Series(String seasonID, String carClassID, boolean needsFiltering, String fileName) {
            this.seasonID = seasonID;
            this.carClassID = carClassID;
            this.needsFiltering = needsFiltering;
            this.fileName = fileName;
        }

        public String getSeasonID() {
            return seasonID;
        }

        public String getCarClassID() {
            return carClassID;
        }

        public boolean isNeedsFiltering() {
            return needsFiltering;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static class RPIracingDriverId implements Serializable {
        private Integer id;
        private Series seriesType;

        public RPIracingDriverId() {
        }

        public RPIracingDriverId(Integer id, Series seriesType) {
            this.id = id;
            this.seriesType = seriesType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RPIracingDriverId that = (RPIracingDriverId) o;
            return Objects.equals(id, that.id) && seriesType == that.seriesType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, seriesType);
        }
    }
}


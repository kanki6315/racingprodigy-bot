package com.racingprodigy.standings_bot.data;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Optional;

@Entity
@Table(name = "iracing_driver")
public class RPIracingDriver {

    @Id
    private Integer id;

    @Nullable
    private Integer position;

    public RPIracingDriver() {
    }

    public RPIracingDriver(Integer id) {
        this.id = id;
    }

    public RPIracingDriver(Integer id, int position) {
        this.id = id;
        this.position = position;
    }

    public Integer getId() {
        return id;
    }

    public Optional<Integer> getPosition() {
        return Optional.ofNullable(position);
    }

    public void setPosition(@Nullable Integer position) {
        this.position = position;
    }
}


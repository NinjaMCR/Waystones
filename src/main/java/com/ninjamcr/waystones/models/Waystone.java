package com.ninjamcr.waystones.models;

import org.bukkit.Location;

import java.util.UUID;

public class Waystone {

    private final UUID id;
    private final String name;
    private final Location location;

    public Waystone(UUID id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }
}
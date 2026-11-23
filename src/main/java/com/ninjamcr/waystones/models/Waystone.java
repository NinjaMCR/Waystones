package com.ninjamcr.waystones.models;

import org.bukkit.Location;

import java.util.UUID;

public class Waystone {

    private final UUID id;
    private final String name;
    private final Location location;
    private final WaystoneIcon icon;

    public Waystone(UUID id, String name, Location location) {
        this(id, name, location, WaystoneIcon.COMPASS);
    }

    public Waystone(UUID id, String name, Location location, WaystoneIcon icon) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.icon = icon;
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

    public WaystoneIcon getIcon() {
        return icon;
    }
}

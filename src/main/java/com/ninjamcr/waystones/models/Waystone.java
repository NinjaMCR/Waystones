package com.ninjamcr.waystones.models;

import org.bukkit.Location;

import java.util.UUID;

public class Waystone {

    private final UUID id;
    private final String name;
    private final Location location;
    private final WaystoneIcon icon;
    private final String description;

    public Waystone(UUID id, String name, Location location, String description) {
        this(id, name, location, WaystoneIcon.COMPASS, description);
    }

    public Waystone(UUID id, String name, Location location, WaystoneIcon icon, String description) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.icon = icon;
        this.description = description;
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

    public String getDescription() {
        return description;
    }
}

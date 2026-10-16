package com.ninjamcr.waystones.utils;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.models.Waystone;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class BlockUtils {
    private final Waystones plugin;

    public BlockUtils(Waystones plugin) {
        this.plugin = plugin;
    }

    public boolean isBlockLocationInList(Location loc) {
        List<Location> wsLocations = new ArrayList<>();
        for(Waystone ws : plugin.waystones) {
            wsLocations.add(ws.getLocation());
        }
        return wsLocations.contains(loc);
    }

    public Waystone getWaystone(Location loc) {
        for (Waystone ws : plugin.waystones) {
            if (ws.getLocation().equals(loc)) {
                return ws;
            }
        }
        return null;
    }
}

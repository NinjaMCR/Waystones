package com.ninjamcr.waystones.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

public class AdvancementUtils {
    public static void awardFirstWaystone(Player player) {
        NamespacedKey key = new NamespacedKey("waystones", "first_waystone");

        Advancement advancement = Bukkit.getAdvancement(key);

        if (advancement == null) {
            Bukkit.getLogger().warning("Waystones advancement not found: " + key);
            return;
        }

        AdvancementProgress progress = player.getAdvancementProgress(advancement);

        if (!progress.isDone()) {
            progress.awardCriteria("waystone_created");
        }
    }
}

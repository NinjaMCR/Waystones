package com.ninjamcr.waystones.utils;

import com.ninjamcr.waystones.models.SelectedWorld;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;

public class AdvancementUtils {
    public static void awardFirstWaystone(Player player, Recipe recipe, NamespacedKey recipeKey) {
        NamespacedKey advancementKey = new NamespacedKey("waystones", "first_waystone");

        AdvancementProgress progress = getAdvancementProgress(advancementKey, player);
        if (progress == null) return;

        if (!progress.isDone() && (recipe instanceof Keyed keyedRecipe)) {
            NamespacedKey craftedKey = keyedRecipe.getKey();
            if (craftedKey.equals(recipeKey)){
                progress.awardCriteria("waystone_created");
            }
        }
    }

    public static void awardFirstJump(Player player){
        NamespacedKey key = new NamespacedKey("waystones", "first_jump");

        AdvancementProgress progress = getAdvancementProgress(key, player);
        if (progress == null) return;

        if (!progress.isDone()) {
            progress.awardCriteria("first_teleport");
        }
    }

    public static void awardInterdimensionalJump(Player player, Location dest, Location startPoint){
        NamespacedKey key = new NamespacedKey("waystones", "first_interdimensional_jump");

        AdvancementProgress progress = getAdvancementProgress(key, player);
        if (progress == null) return;

        if (!dest.getWorld().equals(startPoint.getWorld())){
            progress.awardCriteria("first_interdimensional_teleport");
        }
    }

    public static void awardLongJumps(Player player, Location dest, Location startPoint){
        NamespacedKey longJumpKey = new NamespacedKey("waystones", "long_jump");
        NamespacedKey extraLongJumpKey = new NamespacedKey("waystones", "extra_long_jump");
        startPoint.setZ(0);
        dest.setZ(0);

        AdvancementProgress longJumpProgress = getAdvancementProgress(longJumpKey, player);
        AdvancementProgress extraLongJumpProgress = getAdvancementProgress(extraLongJumpKey, player);
        if (longJumpProgress == null || extraLongJumpProgress == null) return;

        if (!dest.getWorld().equals(startPoint.getWorld())) return;

        double distance = dest.distance(startPoint);
        if (distance >= 15000){
            extraLongJumpProgress.awardCriteria("very_long_distance_teleport");
        }
        if (distance >= 2500){
            longJumpProgress.awardCriteria("long_distance_teleport");
        }
    }

    public static void awardAllDimensionJumps(Player player, Location dest){
        NamespacedKey key = new NamespacedKey("waystones", "every_dimension_jump");

        AdvancementProgress progress = getAdvancementProgress(key, player);
        if (progress == null) return;

        SelectedWorld destinationWorld = SelectedWorld.valueOf(dest.getWorld().getName());
        switch (destinationWorld) {
            case world -> progress.awardCriteria("overworld_teleport");
            case world_nether -> progress.awardCriteria("nether_teleport");
            case world_the_end -> progress.awardCriteria("end_teleport");
            default -> throw new IllegalStateException("Unexpected value: " + destinationWorld);
        }
    }

    private static AdvancementProgress getAdvancementProgress(NamespacedKey key, Player player){
        Advancement advancement = Bukkit.getAdvancement(key);

        if (advancement == null) {
            return null;
        }

        return player.getAdvancementProgress(advancement);
    }
}

package com.ninjamcr.waystones.utils;

import com.ninjamcr.waystones.Waystones;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class WaystoneRecipeManager {
    private final Waystones plugin;
    private final NamespacedKey recipeKey;
    private final NamespacedKey itemKey;

    public WaystoneRecipeManager(Waystones plugin) {
        this.plugin = plugin;
        this.recipeKey = new NamespacedKey(plugin, "waystone_recipe");
        this.itemKey = new NamespacedKey(plugin, "is_waystone");
    }

    public void registerIfMissing() {
        if (Bukkit.getRecipe(recipeKey) != null) {
            return;
        }

        String name = plugin.getConfig().getString("waystone-display-name", "§bWaystone");
        ItemStack result = new ItemStack(Material.LODESTONE);
        ItemMeta meta = result.getItemMeta();
        if (meta == null) {
            plugin.getLogger().severe("Could not create Waystone recipe item metadata.");
            return;
        }
        meta.setDisplayName(name);
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BOOLEAN, true);
        result.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);
        recipe.shape("III", "IEI", "III");
        recipe.setIngredient('I', Material.DIAMOND);
        recipe.setIngredient('E', Material.ENDER_EYE);

        if (!Bukkit.addRecipe(recipe)) {
            plugin.getLogger().warning("Failed to register Waystone crafting recipe.");
        }
    }

    public void startRestoreCheck() {
        // This is a cheap lookup, used to restore the recipe in the event it is unloaded by a resource reload from another plugin.
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (Bukkit.getRecipe(recipeKey) == null) {
                plugin.getLogger().info("Restoring Waystone crafting recipe after resource reload.");
                registerIfMissing();
            }
        }, 40L, 40L);
    }
}

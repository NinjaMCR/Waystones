package com.ninjamcr.waystones;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class Waystones extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // Save and load the config.yml file
        saveDefaultConfig();

        // Setup SQLite
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        // Load pre-existing waystones from .db file into memory cache
        WaystoneListener.waystones.addAll(databaseManager.loadWaystones());

        // Bind System Registrations
        registerWaystoneRecipe();
        getServer().getPluginManager().registerEvents(new WaystoneListener(this), this);

        getLogger().info("Waystone Plugin successfully loaded!");
    }

    @Override
    public void onDisable() {
        // Close DB connections nicely on stops to prevent corruption
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void registerWaystoneRecipe() {
        String customName = getConfig().getString("waystone-display-name", "§bWaystone");

        ItemStack waystoneItem = new ItemStack(Material.LODESTONE);
        ItemMeta meta = waystoneItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(customName);
            NamespacedKey key = new NamespacedKey(this, "is_waystone");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            waystoneItem.setItemMeta(meta);
        }

        NamespacedKey recipeKey = new NamespacedKey(this, "waystone_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, waystoneItem);
        recipe.shape("III", "IEI", "III");
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setIngredient('E', Material.ENDER_EYE);

        Bukkit.addRecipe(recipe);
    }
}

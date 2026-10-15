package com.ninjamcr.waystones;

import com.ninjamcr.waystones.database.DatabaseManager;
import com.ninjamcr.waystones.listeners.BlockListener;
import com.ninjamcr.waystones.listeners.InventoryListener;
import com.ninjamcr.waystones.listeners.PlayerListener;
import com.ninjamcr.waystones.models.Waystone;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Waystones extends JavaPlugin {

    private DatabaseManager databaseManager;
    public List<Waystone> waystones = new ArrayList<>() {};

    @Override
    public void onEnable() {
        // Save and load the config.yml file
        saveDefaultConfig();

        String version = Bukkit.getMinecraftVersion();

        if (!version.startsWith("26.")) {
            getLogger().severe("Unsupported Minecraft version: " + version);
            getLogger().severe("Waystones requires Minecraft 26.x");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Setup SQLite
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        // Load pre-existing waystones from .db file into memory cache
        waystones.addAll(databaseManager.loadWaystones());

        // Listeners
        BlockListener blockListener = new BlockListener(this);
        InventoryListener inventoryListener = new InventoryListener(this);
        PlayerListener playerListener = new PlayerListener(this);

        // Bind System Registrations
        registerWaystoneRecipe();
        getServer().getPluginManager().registerEvents(blockListener, this);
        getServer().getPluginManager().registerEvents(inventoryListener, this);
        getServer().getPluginManager().registerEvents(playerListener, this);

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
        recipe.setIngredient('I', Material.DIAMOND);
        recipe.setIngredient('E', Material.ENDER_EYE);

        Bukkit.addRecipe(recipe);
    }
}

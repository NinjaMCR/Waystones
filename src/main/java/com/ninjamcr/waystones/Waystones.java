package com.ninjamcr.waystones;

import com.ninjamcr.waystones.database.DatabaseManager;
import com.ninjamcr.waystones.listeners.BlockListener;
import com.ninjamcr.waystones.listeners.InventoryListener;
import com.ninjamcr.waystones.listeners.PlayerListener;
import com.ninjamcr.waystones.models.Waystone;
import com.ninjamcr.waystones.utils.DatapackLoader;
import com.ninjamcr.waystones.utils.UpdateChecker;
import com.ninjamcr.waystones.utils.WaystoneRecipeManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Waystones extends JavaPlugin {
    private DatabaseManager databaseManager;
    private WaystoneRecipeManager recipeManager;
    public List<Waystone> waystones = new ArrayList<>();

    @Override
    public void onLoad() {
        // On Paper/Purpur the paper-plugin.yml bootstrapper registers the datapack directly from the jar.
        // Spigot requires a lot more work to copy the datapack into the world
        if (!isPaperServer()) {
            DatapackLoader.installSpigotDatapack(this);
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getDescription().getVersion().contains("beta")) {
            getLogger().warning("WARNING: You are using a beta version which may be unstable");
        } else {
            UpdateChecker updateChecker = new UpdateChecker(this);
            updateChecker.CheckForUpdates();
        }

        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        waystones.addAll(databaseManager.loadWaystones());

        BlockListener blockListener = new BlockListener(this);
        InventoryListener inventoryListener = new InventoryListener(this);
        PlayerListener playerListener = new PlayerListener(this);

        recipeManager = new WaystoneRecipeManager(this);
        recipeManager.registerIfMissing();
        recipeManager.startRestoreCheck();

        getServer().getPluginManager().registerEvents(blockListener, this);
        getServer().getPluginManager().registerEvents(inventoryListener, this);
        getServer().getPluginManager().registerEvents(playerListener, this);

        if (!isPaperServer()) {
            DatapackLoader.activateSpigotDatapack(this);
        } else {
            getLogger().info("Paper datapack is provided by the bootstrapper (waystones/provided).");
        }
        getLogger().info("Waystone Plugin successfully loaded!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void registerWaystoneRecipe() {
        if (recipeManager == null) {
            recipeManager = new WaystoneRecipeManager(this);
        }
        recipeManager.registerIfMissing();
    }

    public static boolean isPaperServer() {
        try {
            Class.forName("io.papermc.paper.plugin.bootstrap.PluginBootstrap", false, DatapackLoader.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}

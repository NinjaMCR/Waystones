package com.ninjamcr.waystones;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class Waystones extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register the recipe for waystones and the new listener that handles all interactions
        CreateRecipe();
        getServer().getPluginManager().registerEvents(new WaystoneListener(this), this);
        getLogger().info("Waystones for 26.2 has enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Waystones has disabled");
    }

    public void CreateRecipe() {
        // 1. Create the Custom Item (Lodestone base)
        ItemStack waystoneItem = new ItemStack(Material.LODESTONE);
        ItemMeta meta = waystoneItem.getItemMeta();

        if (meta != null) {
            meta.customName(Component.text("§bWaystone")); // Cyan color name

            // Inject a unique tag so we can recognize it as a Waystone later
            NamespacedKey key = new NamespacedKey(this, "is_waystone");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

            waystoneItem.setItemMeta(meta);
        }

        // 2. Define the Crafting Recipe
        NamespacedKey recipeKey = new NamespacedKey(this, "waystone_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, waystoneItem);

        // Recipe shape: Eye of ender surrounded by iron blocks
        recipe.shape("III", "IEI", "III");
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setIngredient('E', Material.ENDER_EYE);

        // 3. Register to server
        Bukkit.addRecipe(recipe);
    }
}

package com.ninjamcr.waystones.listeners;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.interaction.WaystoneInteraction;
import com.ninjamcr.waystones.utils.AdvancementUtils;
import com.ninjamcr.waystones.utils.BlockUtils;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerListener implements Listener {
    private final WaystoneInteraction waystoneInteraction;
    private final BlockUtils blockUtils;
    private final Waystones plugin;

    public PlayerListener(Waystones plugin) {
        this.waystoneInteraction = new WaystoneInteraction(plugin);
        this.blockUtils = new BlockUtils(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event){
        NamespacedKey recipeKey = new NamespacedKey(plugin, "waystone_recipe");
        Recipe recipe = event.getRecipe();
        Player player = (Player)event.getWhoClicked();

        if ((recipe instanceof Keyed keyedRecipe)) {
            NamespacedKey key = keyedRecipe.getKey();
            if (key.equals(recipeKey)){
                AdvancementUtils.awardFirstWaystone(player);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() == Material.LODESTONE) {
            if (blockUtils.isBlockLocationInList(block.getLocation())) {
                event.setCancelled(true);
                waystoneInteraction.openWorldSelectorGUI(player);
            }
        }
    }
}

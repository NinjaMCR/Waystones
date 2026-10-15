package com.ninjamcr.waystones.listeners;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.interaction.WaystoneInteraction;
import com.ninjamcr.waystones.models.SelectedWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class InventoryListener implements Listener {
    private final Waystones plugin;
    private final WaystoneInteraction waystoneInteraction;

    public InventoryListener(Waystones plugin) {
        this.plugin = plugin;
        this.waystoneInteraction = new WaystoneInteraction(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String worldSelectTitle = plugin.getConfig().getString("world-selector-title", "World Selector");
        String wsNetTitle = plugin.getConfig().getString("gui-title", "Waystone Network");
        if (!event.getView().getTitle().equals(wsNetTitle) && !event.getView().getTitle().equals(worldSelectTitle)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemMeta meta = clicked.getItemMeta();

        if (meta != null) {
            NamespacedKey destKey = new NamespacedKey(plugin, "dest_world");
            NamespacedKey coordsKey = new NamespacedKey(plugin, "dest_coords");

            // World select path
            if(event.getView().getTitle().equals(worldSelectTitle)) {
                NamespacedKey overworldKey = new NamespacedKey(plugin, "overworld");
                NamespacedKey netherKey = new NamespacedKey(plugin, "nether");
                NamespacedKey endKey = new NamespacedKey(plugin, "end");
                if (meta.getPersistentDataContainer().has(overworldKey, PersistentDataType.STRING)) {
                    waystoneInteraction.openWaystoneMenu(player, SelectedWorld.world, 0);
                } else if (meta.getPersistentDataContainer().has(netherKey, PersistentDataType.STRING)) {
                    waystoneInteraction.openWaystoneMenu(player, SelectedWorld.world_nether, 0);
                } else if (meta.getPersistentDataContainer().has(endKey, PersistentDataType.STRING)) {
                    waystoneInteraction.openWaystoneMenu(player, SelectedWorld.world_the_end, 0);
                }
            }

            // Navigation paths
            if (event.getView().getTitle().equals(wsNetTitle)) {
                NamespacedKey actionKey = new NamespacedKey(plugin, "waystone_gui_action");

                // Navigation button
                String action = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);

                if (action != null) {
                    // Back to world selector
                    if (action.equals("back")) {
                        waystoneInteraction.openWorldSelectorGUI(player);
                        return;
                    }

                    // Next / previous page
                    if (action.equals("next") || action.equals("previous")) {

                        NamespacedKey pageKey = new NamespacedKey(plugin, "waystone_gui_page");
                        NamespacedKey worldKey = new NamespacedKey(plugin, "waystone_gui_world");

                        Integer page = meta.getPersistentDataContainer().get(pageKey, PersistentDataType.INTEGER);
                        String worldName = meta.getPersistentDataContainer().get(worldKey, PersistentDataType.STRING);

                        if (page != null && worldName != null) {
                            SelectedWorld selectedWorld = SelectedWorld.valueOf(worldName);
                            waystoneInteraction.openWaystoneMenu(player, selectedWorld, page);
                        }
                        return;
                    }
                }
            }

            // Waystone network path
            if (event.getView().getTitle().equals(wsNetTitle)){
                if (meta.getPersistentDataContainer().has(destKey, PersistentDataType.STRING)) {
                    String worldName = meta.getPersistentDataContainer().get(destKey, PersistentDataType.STRING);
                    int[] coords = meta.getPersistentDataContainer().get(coordsKey, PersistentDataType.INTEGER_ARRAY);

                    if (worldName != null && coords != null) {
                        Location target = new Location(Bukkit.getWorld(worldName), coords[0] + 0.5, coords[1] + 1.0, coords[2] + 0.5);
                        player.closeInventory();
                        player.teleport(target);

                        String tpMsg = plugin.getConfig().getString("messages.teleport", "Teleported!");
                        player.sendMessage(tpMsg);
                    }
                }
            }
        }
    }
}

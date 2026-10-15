package com.ninjamcr.waystones.interaction;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.models.SelectedWorld;
import com.ninjamcr.waystones.models.Waystone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class WaystoneInteraction {
    private final Waystones plugin;

    public WaystoneInteraction(Waystones plugin) {
        this.plugin = plugin;
    }

    public void openWorldSelectorGUI(Player player) {
        String title = plugin.getConfig().getString("world-selector-title", "World Selector");
        Inventory worldSelectorGui = Bukkit.createInventory(null, 9, title);

        NamespacedKey overworldKey = new NamespacedKey(plugin, "overworld");
        ItemStack overworldSelector = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta overworldMeta = overworldSelector.getItemMeta();
        overworldMeta.setDisplayName("§aOverworld");
        overworldMeta.getPersistentDataContainer().set(overworldKey, PersistentDataType.STRING, "overworld");
        overworldSelector.setItemMeta(overworldMeta);

        NamespacedKey netherKey = new NamespacedKey(plugin, "nether");
        ItemStack netherSelector = new ItemStack(Material.NETHERRACK);
        ItemMeta netherMeta = netherSelector.getItemMeta();
        netherMeta.setDisplayName("§cNether");
        netherMeta.getPersistentDataContainer().set(netherKey, PersistentDataType.STRING, "nether");
        netherSelector.setItemMeta(netherMeta);

        NamespacedKey endKey = new NamespacedKey(plugin, "end");
        ItemStack endSelector = new ItemStack(Material.END_STONE);
        ItemMeta endMeta = endSelector.getItemMeta();
        endMeta.setDisplayName("§9End");
        endMeta.getPersistentDataContainer().set(endKey, PersistentDataType.STRING, "end");
        endSelector.setItemMeta(endMeta);

        worldSelectorGui.setItem(0, overworldSelector);
        worldSelectorGui.setItem(4, netherSelector);
        worldSelectorGui.setItem(8, endSelector);

        player.openInventory(worldSelectorGui);
    }

    public void openWaystoneMenu(Player player, SelectedWorld selectedWorld, int page) {
        String title = plugin.getConfig().getString("gui-title", "Waystone Network");
        Inventory gui = Bukkit.createInventory(null, 36, title);
        int waystonesPerPage = 27;

        // Get all waystones for selected world
        List<Waystone> worldWaystones = new ArrayList<>();
        for (Waystone waystone : plugin.waystones) {
            Location loc = waystone.getLocation();
            if (!loc.getWorld().getName().equals(selectedWorld.toString())) {
                continue;
            }
            worldWaystones.add(waystone);
        }

        // Calculate which waystones to display
        int start = page * waystonesPerPage;
        int end = Math.min(start + waystonesPerPage, worldWaystones.size());

        int slot = 0;
        for (int i = start; i < end; i++) {
            Waystone waystone = worldWaystones.get(i);
            Location loc = waystone.getLocation();

            ItemStack teleporter = new ItemStack(Material.COMPASS);
            ItemMeta teleporterMeta = teleporter.getItemMeta();

            // Add teleporter item to gui
            if (teleporterMeta != null) {
                teleporterMeta.setDisplayName("§b" + waystone.getName() + " at: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());

                NamespacedKey destKey = new NamespacedKey(plugin, "dest_world");
                teleporterMeta.getPersistentDataContainer().set(destKey, PersistentDataType.STRING, loc.getWorld().getName());

                NamespacedKey coordsKey = new NamespacedKey(plugin, "dest_coords");
                teleporterMeta.getPersistentDataContainer().set(coordsKey, PersistentDataType.INTEGER_ARRAY, new int[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()});

                teleporter.setItemMeta(teleporterMeta);
            }
            gui.setItem(slot++, teleporter);
        }

        // Previous page selection
        if (page > 0) {
            ItemStack previous = new ItemStack(Material.ARROW);
            ItemMeta previousMeta = previous.getItemMeta();

            if (previousMeta != null) {
                previousMeta.setDisplayName("§ePrevious Page");
                NamespacedKey actionKey = new NamespacedKey(plugin, "waystone_gui_action");
                NamespacedKey pageKey = new NamespacedKey(plugin, "waystone_gui_page");
                NamespacedKey worldKey = new NamespacedKey(plugin, "waystone_gui_world");
                previousMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "previous");
                previousMeta.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, page - 1);
                previousMeta.getPersistentDataContainer().set(worldKey, PersistentDataType.STRING, selectedWorld.toString());
                previous.setItemMeta(previousMeta);
            }
            gui.setItem(27, previous);
        }

        // Next page selection
        if (end < worldWaystones.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();

            if (nextMeta != null) {
                nextMeta.setDisplayName("§eNext Page");
                NamespacedKey actionKey = new NamespacedKey(plugin, "waystone_gui_action");
                NamespacedKey pageKey = new NamespacedKey(plugin, "waystone_gui_page");
                NamespacedKey worldKey = new NamespacedKey(plugin, "waystone_gui_world");
                nextMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "next");
                nextMeta.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, page + 1);
                nextMeta.getPersistentDataContainer().set(worldKey, PersistentDataType.STRING, selectedWorld.toString());
                next.setItemMeta(nextMeta);
            }
            gui.setItem(35, next);
        }

        // Back to world selection
        ItemStack back = new ItemStack(Material.ENDER_EYE);
        ItemMeta backMeta = back.getItemMeta();

        if (backMeta != null) {
            backMeta.setDisplayName("§eBack to World Selector");

            NamespacedKey actionKey = new NamespacedKey(plugin, "waystone_gui_action");
            backMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "back");

            back.setItemMeta(backMeta);
        }

        gui.setItem(31, back);

        player.openInventory(gui);
    }
}

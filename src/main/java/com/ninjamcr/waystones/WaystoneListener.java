package com.ninjamcr.waystones;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class WaystoneListener implements Listener {

    private final JavaPlugin plugin;
    public static final List<Location> waystoneLocations = new ArrayList<>();

    public WaystoneListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Check that the placed block is a waystone and add its location to the temporary storage
    @EventHandler
    public void OnBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        NamespacedKey key = new NamespacedKey(plugin, "is_waystone");

        if (item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN) && item.getItemMeta() != null){
            Block block = event.getBlockPlaced();

            waystoneLocations.add(block.getLocation());
            event.getPlayer().sendMessage(ChatColor.GREEN + "You have placed a waystone");
        }
    }

    @EventHandler
    public void OnPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (block != null && block.getType() == Material.LODESTONE && event.getAction() == Action.RIGHT_CLICK_BLOCK && waystoneLocations.contains(block.getLocation())) {
            event.setCancelled(true);

            openWaystoneMenu(player, block.getLocation());
        }
    }

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent event) {
        // Make sure we're in the right GUI and don't let the player take the compass out
        if(!event.getView().getTitle().equals("§8Select a Waystone Network Destination")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.COMPASS) return;

        ItemMeta meta = clicked.getItemMeta();

        // Check the item metadata and tp player to destination
        if (meta != null) {
            NamespacedKey destKey = new NamespacedKey(plugin, "dest_world");
            NamespacedKey coordsKey = new NamespacedKey(plugin, "dest_coords");
            String destWorld;
            int[] destCoords;

            if(meta.getPersistentDataContainer().has(destKey, PersistentDataType.STRING) && meta.getPersistentDataContainer().has(coordsKey, PersistentDataType.INTEGER_ARRAY)) {
                destWorld = meta.getPersistentDataContainer().get(destKey, PersistentDataType.STRING);
                destCoords = meta.getPersistentDataContainer().get(coordsKey, PersistentDataType.INTEGER_ARRAY);

                // TP player to destination
                if (destWorld != null && destCoords != null) {
                    org.bukkit.Location target = new org.bukkit.Location(Bukkit.getWorld(destWorld), destCoords[0] + 0.5, destCoords[1] + 1.0, destCoords[2] + 0.5);

                    player.closeInventory();
                    player.teleport(target);
                    player.sendMessage("§aWoosh! Teleported to your selected Waystone.");
                }
            }
        }
    }

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block != null && block.getType() == Material.LODESTONE && waystoneLocations.contains(block.getLocation())) {
            waystoneLocations.remove(block.getLocation());
        }
    }


    private void openWaystoneMenu(Player player, org.bukkit.Location currentLoc) {
        // Create an inventory window (GUI) with 9 slots
        Inventory gui = Bukkit.createInventory(null, 9, "§8Select a Waystone Network Destination");

        int slot = 0;
        for (org.bukkit.Location loc : waystoneLocations) {
            if (loc.equals(currentLoc)) continue; // Don't show the current waystone
            if (slot >= 9) break; // Limit to 9 options for this basic script

            ItemStack teleporter = new ItemStack(Material.COMPASS);
            ItemMeta meta = teleporter.getItemMeta();
            if (meta != null) {
                meta.customName(Component.text("§bWaystone at: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()));

                // Hidden destination data tag attached to the GUI item
                NamespacedKey destKey = new NamespacedKey(plugin, "dest_world");
                meta.getPersistentDataContainer().set(destKey, PersistentDataType.STRING, loc.getWorld().getName());

                // Store coordinates as integer arrays
                NamespacedKey coordsKey = new NamespacedKey(plugin, "dest_coords");
                meta.getPersistentDataContainer().set(coordsKey, PersistentDataType.INTEGER_ARRAY, new int[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()});

                teleporter.setItemMeta(meta);
            }
            gui.setItem(slot++, teleporter);
        }

        player.openInventory(gui);
    }
}

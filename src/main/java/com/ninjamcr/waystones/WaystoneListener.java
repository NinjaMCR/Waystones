package com.ninjamcr.waystones;

import com.ninjamcr.waystones.models.SelectedWorld;
import com.ninjamcr.waystones.models.Waystone;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WaystoneListener implements Listener {

    private final Waystones plugin;
    public static final List<Waystone> waystones = new ArrayList<>();


    public WaystoneListener(Waystones plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        NamespacedKey key = new NamespacedKey(plugin, "is_waystone");

        if (item.getItemMeta() != null && item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN)) {
            Block block = event.getBlockPlaced();
            Location loc = block.getLocation();
            Player player = event.getPlayer();

            openWaystoneCreationDialog(player, loc);

            String placedMsg = plugin.getConfig().getString("messages.placed", "You placed a Waystone!");
            event.getPlayer().sendMessage(placedMsg);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.LODESTONE && isBlockLocationInList(block.getLocation())) {
            Location loc = block.getLocation();
            Waystone ws = getWaystone(loc);
            // Clear from cache + SQLite DB
            waystones.remove(ws);
            plugin.getDatabaseManager().deleteWaystone(ws);

            String brokenMsg = plugin.getConfig().getString("messages.broken", "Waystone broken.");
            event.getPlayer().sendMessage(brokenMsg);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() == Material.LODESTONE) {
            if (isBlockLocationInList(block.getLocation())) {
                event.setCancelled(true);
                openWorldSelectorGUI(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String worldSelectTitle = plugin.getConfig().getString("gui-title", "World Selector");
        String wsNetTitle = plugin.getConfig().getString("gui-title", "Waystone Network");
        if (!event.getView().getTitle().equals(wsNetTitle) && !event.getView().getTitle().equals(worldSelectTitle)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemMeta meta = clicked.getItemMeta();

        if (meta != null) {
            NamespacedKey overworldKey = new NamespacedKey(plugin, "overworld");
            NamespacedKey netherKey = new NamespacedKey(plugin, "nether");
            NamespacedKey endKey = new NamespacedKey(plugin, "end");
            NamespacedKey destKey = new NamespacedKey(plugin, "dest_world");
            NamespacedKey coordsKey = new NamespacedKey(plugin, "dest_coords");

            if (meta.getPersistentDataContainer().has(overworldKey, PersistentDataType.STRING)) {
                openWaystoneMenu(player, SelectedWorld.world);
            } else if (meta.getPersistentDataContainer().has(netherKey, PersistentDataType.STRING)) {
                openWaystoneMenu(player, SelectedWorld.world_nether);
            } else if (meta.getPersistentDataContainer().has(endKey, PersistentDataType.STRING)) {
                openWaystoneMenu(player, SelectedWorld.world_the_end);
            } else if (meta.getPersistentDataContainer().has(destKey, PersistentDataType.STRING)) {
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

    public void openWaystoneCreationDialog(Player player, Location loc) {

        Dialog dialog = Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text("Create Waystone"))
                .inputs(List.of(DialogInput.text("waystone_name", Component.text("Waystone Name")).width(300).labelVisible(true).initial("").maxLength(32).build()))
                .build())
                    .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("Create"))

                            .action(DialogAction.customClick((view, audience) -> {

                                if (!(audience instanceof Player submittingPlayer)) {
                                    return;
                                }

                                String waystoneName = view.getText("waystone_name");

                                // Save waystone location and name to memory and db
                                Waystone waystone = new Waystone(UUID.randomUUID(), waystoneName, loc);
                                waystones.add(waystone);
                                plugin.getDatabaseManager().saveWaystone(waystone);

                            },
                            ClickCallback.Options.builder().uses(1).build()))
                        .build(),
                        ActionButton.builder(Component.text("Cancel")).build())
                    )
        );

        player.showDialog(dialog);
    }

    private void openWorldSelectorGUI(Player player) {
        String title = plugin.getConfig().getString("gui-title", "World Selector");
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

    private void openWaystoneMenu(Player player, SelectedWorld selectedWorld) {
        String title = plugin.getConfig().getString("gui-title", "Waystone Network");
        Inventory gui = Bukkit.createInventory(null, 36, title);

        int slot = 0;
        for (Waystone waystone : waystones) {
            Location loc = waystone.getLocation();
            if (!loc.getWorld().getName().equals(selectedWorld.toString())) continue;
            if (slot >= 27) break;

            ItemStack teleporter = new ItemStack(Material.COMPASS);
            ItemMeta teleporterMeta = teleporter.getItemMeta();
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
        player.openInventory(gui);
    }

    private boolean isBlockLocationInList(Location loc) {
        List<Location> wsLocations = new ArrayList<>();
        for(Waystone ws : waystones) {
            wsLocations.add(ws.getLocation());
        }
        return wsLocations.contains(loc);
    }

    private Waystone getWaystone(Location loc) {
        for (Waystone ws : waystones) {
            if (ws.getLocation().equals(loc)) {
                return ws;
            }
        }
        return null;
    }
}

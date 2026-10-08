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
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Iterator;
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
        NamespacedKey key = new NamespacedKey(plugin, "is_waystone");
        ItemMeta meta = event.getItemInHand().getItemMeta();

        // Check that what we are placing is a waystone
        if (meta.getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN) == null) {
            return;
        }

        event.setCancelled(true);

        openWaystoneCreationDialog(event.getPlayer(), event.getBlockPlaced().getLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.LODESTONE && isBlockLocationInList(block.getLocation())) {
            event.setDropItems(false);

            handleWaystoneBreak(block);

            String brokenMsg = plugin.getConfig().getString(
                    "messages.broken",
                    "Waystone broken."
            );

            event.getPlayer().sendMessage(brokenMsg);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();

            if (block.getType() == Material.LODESTONE && isBlockLocationInList(block.getLocation())) {
                iterator.remove();
                handleWaystoneBreak(block);
                block.setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();

            if (block.getType() == Material.LODESTONE && isBlockLocationInList(block.getLocation())) {
                iterator.remove();
                handleWaystoneBreak(block);
                block.setType(Material.AIR);
            }
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
                    openWaystoneMenu(player, SelectedWorld.world, 0);
                } else if (meta.getPersistentDataContainer().has(netherKey, PersistentDataType.STRING)) {
                    openWaystoneMenu(player, SelectedWorld.world_nether, 0);
                } else if (meta.getPersistentDataContainer().has(endKey, PersistentDataType.STRING)) {
                    openWaystoneMenu(player, SelectedWorld.world_the_end, 0);
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
                        openWorldSelectorGUI(player);
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
                            openWaystoneMenu(player, selectedWorld, page);
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

    private void handleWaystoneBreak(Block block) {
        if (block.getType() != Material.LODESTONE) {
            return;
        }

        if (!isBlockLocationInList(block.getLocation())) {
            return;
        }

        Location loc = block.getLocation();
        Waystone ws = getWaystone(loc);

        plugin.getLogger().info(
                "Waystone '" + ws.getName() + "' at " +
                        loc.getWorld().getName() + " " +
                        loc.getBlockX() + ", " +
                        loc.getBlockY() + ", " +
                        loc.getBlockZ() +
                        " was destroyed and removed from the network."
        );

        // Clear from cache + SQLite DB
        waystones.remove(ws);
        plugin.getDatabaseManager().deleteWaystone(ws);

        // Drop a waystone instead of a lodestone
        String customName = plugin.getConfig().getString(
                "waystone-display-name",
                "§bWaystone"
        );

        ItemStack waystoneItem = new ItemStack(Material.LODESTONE);
        ItemMeta meta = waystoneItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(customName);

            NamespacedKey key = new NamespacedKey(plugin, "is_waystone");
            meta.getPersistentDataContainer().set(
                    key,
                    PersistentDataType.BOOLEAN,
                    true
            );

            waystoneItem.setItemMeta(meta);
        }

        block.getWorld().dropItemNaturally(loc, waystoneItem);
    }

    private void openWaystoneCreationDialog(Player player, Location loc) {

        Dialog dialog = Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text("Create Waystone"))
                .inputs(List.of(DialogInput.text("waystone_name", Component.text("Waystone Name")).width(300).labelVisible(true).initial("").maxLength(32).build()))
                .build()
            )
                .type(DialogType.confirmation(
                    ActionButton.builder(Component.text("Create"))
                        .action(DialogAction.customClick((view, audience) -> {
                            if (!(audience instanceof Player submittingPlayer)) {
                                return;
                            }
                            String waystoneName = view.getText("waystone_name");

                            // Actually place the lodestone and remove the waystone from the player's inventory
                            NamespacedKey key = new NamespacedKey(plugin, "is_waystone");
                            if (submittingPlayer.getGameMode() != GameMode.CREATIVE) {
                                for (ItemStack item : submittingPlayer.getInventory().getContents()) {
                                    if (item == null || item.getType() != Material.LODESTONE) {
                                        continue;
                                    }

                                    ItemMeta meta = item.getItemMeta();
                                    if (meta == null) {
                                        continue;
                                    }

                                    Boolean isWaystone = meta.getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN);
                                    if (Boolean.TRUE.equals(isWaystone)) {
                                        item.setAmount(item.getAmount() - 1);
                                        break;
                                    }
                                }
                            }
                            loc.getBlock().setType(Material.LODESTONE);

                            // Save waystone location and name to memory and db
                            Waystone waystone = new Waystone(UUID.randomUUID(), waystoneName, loc);
                            waystones.add(waystone);
                            plugin.getDatabaseManager().saveWaystone(waystone);

                            // Send confirmation message
                            String placedMsg = plugin.getConfig().getString("messages.placed", "You placed a Waystone!");
                            player.sendMessage(placedMsg);
                        },
                        ClickCallback.Options.builder().uses(1).build())).build(),
                        ActionButton.builder(Component.text("Cancel")).build()
                        )
                )
        );

        player.showDialog(dialog);
    }

    private void openWorldSelectorGUI(Player player) {
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

    private void openWaystoneMenu(Player player, SelectedWorld selectedWorld, int page) {
        String title = plugin.getConfig().getString("gui-title", "Waystone Network");
        Inventory gui = Bukkit.createInventory(null, 36, title);
        int waystonesPerPage = 27;

        // Get all waystones for selected world
        List<Waystone> worldWaystones = new ArrayList<>();
        for (Waystone waystone : waystones) {
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

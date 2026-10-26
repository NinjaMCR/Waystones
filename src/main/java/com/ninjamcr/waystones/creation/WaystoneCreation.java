package com.ninjamcr.waystones.creation;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.models.Waystone;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class WaystoneCreation {
    public static final String INPUT_NAME = "waystone_name";
    private static final long PENDING_TIMEOUT_MILLIS = 120_000L;

    private final Waystones plugin;
    private final NamespacedKey isWaystoneKey;
    private final Map<UUID, PendingPlacement> pendingPlacements = new HashMap<>();
    private final WaystoneDialogAdapter dialogAdapter;

    public WaystoneCreation(Waystones plugin) {
        this.plugin = plugin;
        this.isWaystoneKey = new NamespacedKey(plugin, "is_waystone");
        this.dialogAdapter = createAdapter();
        this.dialogAdapter.registerHandlers(plugin);
        plugin.getLogger().info("Waystone creation UI: " + dialogAdapter.getClass().getSimpleName());
    }

    public Waystones getPlugin() {
        return plugin;
    }

    public void openWaystoneCreationDialog(Player player, Location loc) {
        if (loc.getWorld() == null) {
            player.sendMessage("Cannot create a Waystone in an unknown world.");
            return;
        }

        UUID playerId = player.getUniqueId();
        pendingPlacements.put(playerId,
                new PendingPlacement(loc.clone(), System.currentTimeMillis()));
        try {
            dialogAdapter.open(player);
        } catch (RuntimeException | LinkageError ex) {
            pendingPlacements.remove(playerId);
            plugin.getLogger().log(Level.SEVERE, "Unable to open the Waystone creation dialog", ex);
            player.sendMessage("Unable to open the Waystone dialog.");
        }
    }

    public void confirm(Player player, String submittedName) {
        UUID playerId = player.getUniqueId();
        runOnMainThread(() -> {
            PendingPlacement pending = pendingPlacements.remove(playerId);
            Player onlinePlayer = Bukkit.getPlayer(playerId);
            if (pending == null || onlinePlayer == null || !onlinePlayer.isOnline()) {
                return;
            }
            if (System.currentTimeMillis() - pending.createdAtMillis()
                    > PENDING_TIMEOUT_MILLIS) {
                onlinePlayer.sendMessage("Waystone placement expired. Please try again.");
                return;
            }
            if (submittedName == null) {
                onlinePlayer.sendMessage("Invalid Waystone name. Please try again.");
                return;
            }
            createWaystone(onlinePlayer, pending.location(), submittedName);
        });
    }

    public void cancel(Player player) {
        UUID playerId = player.getUniqueId();
        runOnMainThread(() -> pendingPlacements.remove(playerId));
    }

    public void clearPendingPlacement(UUID playerId) {
        runOnMainThread(() -> pendingPlacements.remove(playerId));
    }

    private void runOnMainThread(Runnable action) {
        if (Bukkit.isPrimaryThread()) {
            action.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, action);
        }
    }

    private WaystoneDialogAdapter createAdapter() {
        // This method is used to determine which platform the waystone creation dialog is running on. As the APIs are different for spigot and paper
        boolean paper;
        try {
            Class.forName("io.papermc.paper.dialog.Dialog", false, getClass().getClassLoader());
            paper = true;
        } catch (ClassNotFoundException ex) {
            paper = false;
        }

        String className = "com.ninjamcr.waystones.creation." + (paper ? "PaperDialogAdapter" : "SpigotDialogAdapter");
        try {
            Class<? extends WaystoneDialogAdapter> adapterClass = Class.forName(className, true, getClass().getClassLoader()).asSubclass(WaystoneDialogAdapter.class);
            Constructor<? extends WaystoneDialogAdapter> constructor = adapterClass.getConstructor(WaystoneCreation.class);
            return constructor.newInstance(this);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | LinkageError ex) {
            throw new IllegalStateException("Cannot initialize " + className + " on this server. Check the compatible server API and plugin JAR", ex);
        }
    }

    private void createWaystone(Player player, Location location, String rawName) {
        if (!player.isOnline() || location.getWorld() == null) {
            return;
        }

        String name = rawName.trim();
        if (name.isEmpty() || name.length() > 32) {
            player.sendMessage("Waystone names must contain 1-32 characters.");
            return;
        }

        Block block = location.getBlock();
        if (!block.getType().isAir()) {
            player.sendMessage("The Waystone placement location is no longer empty.");
            return;
        }

        int itemSlot = -1;
        if (player.getGameMode() != GameMode.CREATIVE) {
            itemSlot = findCustomWaystoneSlot(player);
            if (itemSlot < 0) {
                player.sendMessage("You need a Waystone item to place this block.");
                return;
            }
        }

        BlockState previous = block.getState();
        Waystone waystone = new Waystone(UUID.randomUUID(), name, location.clone());

        try {
            block.setType(Material.LODESTONE);
            plugin.waystones.add(waystone);
            plugin.getDatabaseManager().saveWaystone(waystone);
        } catch (RuntimeException ex) {
            plugin.waystones.remove(waystone);
            previous.update(true, false);
            plugin.getLogger().log(Level.SEVERE, "Could not save a new Waystone", ex);
            player.sendMessage("Unable to save your Waystone. No item was consumed.");
            return;
        }

        if (itemSlot >= 0) {
            ItemStack item = player.getInventory().getItem(itemSlot);
            if (item != null) {
                if (item.getAmount() <= 1) {
                    player.getInventory().setItem(itemSlot, null);
                } else {
                    item.setAmount(item.getAmount() - 1);
                    player.getInventory().setItem(itemSlot, item);
                }
            }
        }

        player.sendMessage(plugin.getConfig().getString("messages.placed", "You placed a Waystone!"));
    }

    private int findCustomWaystoneSlot(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item == null || item.getType() != Material.LODESTONE) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta != null && Boolean.TRUE.equals(
                    meta.getPersistentDataContainer().get(isWaystoneKey, PersistentDataType.BOOLEAN))) {
                return slot;
            }
        }
        return -1;
    }

    private record PendingPlacement(Location location, long createdAtMillis) { }
}

package com.ninjamcr.waystones.creation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.models.Waystone;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.dialog.ConfirmationDialog;
import net.md_5.bungee.api.dialog.DialogBase;
import net.md_5.bungee.api.dialog.action.ActionButton;
import net.md_5.bungee.api.dialog.action.CustomClickAction;
import net.md_5.bungee.api.dialog.input.TextInput;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class WaystoneCreation {
    private static final String INPUT_NAME = "waystone_name";
    private static final long PENDING_TIMEOUT_MILLIS = 120_000L;

    private final Waystones plugin;
    private final NamespacedKey createAction;
    private final NamespacedKey cancelAction;
    private final NamespacedKey isWaystoneKey;
    private final Map<UUID, PendingPlacement> pendingPlacements = new HashMap<>();

    public WaystoneCreation(Waystones plugin) {
        this.plugin = plugin;
        this.createAction = new NamespacedKey(plugin, "create_waystone");
        this.cancelAction = new NamespacedKey(plugin, "cancel_waystone");
        this.isWaystoneKey = new NamespacedKey(plugin, "is_waystone");
    }

    public void openWaystoneCreationDialog(Player player, Location loc) {
        if (loc.getWorld() == null) {
            player.sendMessage("Cannot create a Waystone in an unknown world.");
            return;
        }

        TextInput nameInput = new TextInput(
                INPUT_NAME, new TextComponent("Waystone Name")
        ).width(300).labelVisible(true).initial("").maxLength(32);

        DialogBase base = new DialogBase(new TextComponent("Create Waystone"))
                .inputs(List.of(nameInput));

        ActionButton create = new ActionButton(
                new TextComponent("Create"),
                new CustomClickAction(createAction.toString())
        );
        ActionButton cancel = new ActionButton(
                new TextComponent("Cancel"),
                new CustomClickAction(cancelAction.toString())
        );

        ConfirmationDialog dialog = new ConfirmationDialog(base, create, cancel);

        pendingPlacements.put(
                player.getUniqueId(),
                new PendingPlacement(loc.clone(), System.currentTimeMillis())
        );

        try {
            player.showDialog(dialog);
        } catch (RuntimeException ex) {
            pendingPlacements.remove(player.getUniqueId());
            plugin.getLogger().log(Level.SEVERE, "Unable to show the Waystone creation dialog", ex);
            player.sendMessage("Unable to open the Waystone dialog.");
        }
    }

    public boolean isWaystoneDialogAction(NamespacedKey action) {
        return createAction.equals(action) || cancelAction.equals(action);
    }

    public void handleDialogSubmit(Player player, NamespacedKey action, JsonElement data, boolean asynchronous) {
        if (!isWaystoneDialogAction(action)) {
            return;
        }

        boolean cancelled = cancelAction.equals(action);
        String submittedName = cancelled ? null : parseName(data);

        Runnable handle = () -> {
            PendingPlacement pending = pendingPlacements.remove(player.getUniqueId());
            if (pending == null || cancelled || !player.isOnline()) {
                return;
            }

            if (System.currentTimeMillis() - pending.createdAtMillis > PENDING_TIMEOUT_MILLIS) {
                player.sendMessage("Waystone placement expired. Please try again.");
                return;
            }

            if (submittedName == null) {
                player.sendMessage("Invalid Waystone name. Please try again.");
                return;
            }

            createWaystone(player, pending.location, submittedName);
        };

        if (asynchronous) {
            Bukkit.getScheduler().runTask(plugin, handle);
        } else {
            handle.run();
        }
    }

    public void clearPendingPlacement(UUID playerId) {
        pendingPlacements.remove(playerId);
    }

    private String parseName(JsonElement data) {
        if (data == null || !data.isJsonObject()) {
            return null;
        }

        JsonObject fields = data.getAsJsonObject();
        JsonElement name = fields.get(INPUT_NAME);
        if (name == null || !name.isJsonPrimitive() || !name.getAsJsonPrimitive().isString()) {
            return null;
        }

        return name.getAsString();
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

        // The placement may have been occupied since the dialog was opened.
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

    private record PendingPlacement(Location location, long createdAtMillis) {}
}

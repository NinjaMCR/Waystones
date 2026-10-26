package com.ninjamcr.waystones.listeners;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.creation.WaystoneCreation;
import com.ninjamcr.waystones.destruction.WaystoneDestruction;
import com.ninjamcr.waystones.utils.BlockUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCustomClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Iterator;

public class BlockListener implements Listener {

    private final Waystones plugin;
    private final BlockUtils blockUtils;
    private final WaystoneCreation waystoneCreation;
    private final WaystoneDestruction waystoneDestruction;
    private final NamespacedKey isWaystoneKey;

    public BlockListener(Waystones plugin) {
        this.plugin = plugin;
        this.blockUtils = new BlockUtils(plugin);
        this.waystoneCreation = new WaystoneCreation(plugin);
        this.waystoneDestruction = new WaystoneDestruction(plugin);
        this.isWaystoneKey = new NamespacedKey(plugin, "is_waystone");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.canBuild()) {
            return;
        }

        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.LODESTONE) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !Boolean.TRUE.equals(meta.getPersistentDataContainer().get(isWaystoneKey, PersistentDataType.BOOLEAN))) {
            return;
        }

        // Defer placement until the player confirms a name in the dialog.
        event.setCancelled(true);
        waystoneCreation.openWaystoneCreationDialog(event.getPlayer(), event.getBlockPlaced().getLocation());
    }

    @EventHandler
    public void onWaystoneDialogClick(PlayerCustomClickEvent event) {
        if (!waystoneCreation.isWaystoneDialogAction(event.getId())) {
            return;
        }

        waystoneCreation.handleDialogSubmit(event.getPlayer(), event.getId(), event.getData(), event.isAsynchronous());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        waystoneCreation.clearPendingPlacement(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.LODESTONE && blockUtils.isBlockLocationInList(block.getLocation())) {
            event.setDropItems(false);
            waystoneDestruction.handleWaystoneBreak(block);

            String brokenMsg = plugin.getConfig().getString(
                    "messages.broken", "Waystone broken.");
            event.getPlayer().sendMessage(brokenMsg);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();
        waystoneDestruction.BlockDestruction(iterator);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();
        waystoneDestruction.BlockDestruction(iterator);
    }
}

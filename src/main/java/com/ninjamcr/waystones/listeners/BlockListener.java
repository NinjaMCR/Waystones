package com.ninjamcr.waystones.listeners;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.creation.WaystoneCreation;
import com.ninjamcr.waystones.destruction.WaystoneDestruction;
import com.ninjamcr.waystones.interaction.WaystoneInteraction;
import com.ninjamcr.waystones.models.Waystone;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockListener  implements Listener {

    private final Waystones plugin;
    private final BlockUtils blockUtils;
    private final WaystoneCreation waystoneCreation;
    private final WaystoneDestruction waystoneDestruction;

    public BlockListener(Waystones plugin) {
        this.plugin = plugin;
        this.blockUtils = new BlockUtils(plugin);
        this.waystoneCreation = new WaystoneCreation(plugin);
        this.waystoneDestruction = new WaystoneDestruction(plugin);
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

        waystoneCreation.openWaystoneCreationDialog(event.getPlayer(), event.getBlockPlaced().getLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.LODESTONE && blockUtils.isBlockLocationInList(block.getLocation())) {
            event.setDropItems(false);

            waystoneDestruction.handleWaystoneBreak(block);

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
        waystoneDestruction.BlockDestruction(iterator);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();
        waystoneDestruction.BlockDestruction(iterator);
    }
}

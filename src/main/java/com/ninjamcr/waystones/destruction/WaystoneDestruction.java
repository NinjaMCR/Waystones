package com.ninjamcr.waystones.destruction;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.models.Waystone;
import com.ninjamcr.waystones.utils.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.Iterator;

public class WaystoneDestruction {
    private final Waystones plugin;
    private final BlockUtils blockUtils;

    public WaystoneDestruction(Waystones plugin) {
        this.plugin = plugin;
        this.blockUtils = new BlockUtils(plugin);
    }

    public void handleWaystoneBreak(Block block) {
        if (block.getType() != Material.LODESTONE) {
            return;
        }

        if (!blockUtils.isBlockLocationInList(block.getLocation())) {
            return;
        }

        Location loc = block.getLocation();
        Waystone ws = blockUtils.getWaystone(loc);

        plugin.getLogger().info(
                "Waystone '" + ws.getName() + "' at " +
                        loc.getWorld().getName() + " " +
                        loc.getBlockX() + ", " +
                        loc.getBlockY() + ", " +
                        loc.getBlockZ() +
                        " was destroyed and removed from the network."
        );

        // Clear from cache + SQLite DB
        plugin.waystones.remove(ws);
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

    public void BlockDestruction(Iterator<Block> iterator) {
        while (iterator.hasNext()) {
            Block block = iterator.next();

            if (block.getType() == Material.LODESTONE && blockUtils.isBlockLocationInList(block.getLocation())) {
                iterator.remove();
                handleWaystoneBreak(block);
                block.setType(Material.AIR);
            }
        }
    }
}

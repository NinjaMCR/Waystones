package com.ninjamcr.waystones.listeners;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.interaction.WaystoneInteraction;
import com.ninjamcr.waystones.utils.BlockUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener {
    private final Waystones plugin;
    private final WaystoneInteraction waystoneInteraction;
    private final BlockUtils blockUtils;

    public PlayerListener(Waystones plugin) {
        this.plugin = plugin;
        this.waystoneInteraction = new WaystoneInteraction(plugin);
        this.blockUtils = new BlockUtils(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() == Material.LODESTONE) {
            if (blockUtils.isBlockLocationInList(block.getLocation())) {
                event.setCancelled(true);
                waystoneInteraction.openWorldSelectorGUI(player);
            }
        }
    }
}

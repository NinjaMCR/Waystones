package com.ninjamcr.waystones.creation;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.models.WaystoneIcon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

final class WaystoneIconPicker implements Listener {
    private static final WaystoneIcon[] ICONS = WaystoneIcon.values();
    private static final int INVENTORY_SIZE = 54;
    private final Waystones plugin;

    WaystoneIconPicker(Waystones plugin) {
        this.plugin = plugin;
    }

    void open(Player player, Consumer<WaystoneIcon> onSelect) {
        player.openInventory(new PickerInventory(onSelect).getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (!(inventory.getHolder() instanceof PickerInventory picker)) {
            return;
        }

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= ICONS.length
                || (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT)
                || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Bukkit requires inventory changes triggered by a click to run on the next tick.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory().getHolder() != picker) {
                return;
            }
            player.closeInventory();
            picker.onSelect.accept(ICONS[slot]);
        });
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof PickerInventory) {
            event.setCancelled(true);
        }
    }

    private static final class PickerInventory implements InventoryHolder {
        private final Consumer<WaystoneIcon> onSelect;
        private final Inventory inventory;

        private PickerInventory(Consumer<WaystoneIcon> onSelect) {
            this.onSelect = onSelect;
            this.inventory = Bukkit.createInventory(this, INVENTORY_SIZE, "Choose Waystone Icon");
            for (int slot = 0; slot < ICONS.length; slot++) {
                inventory.setItem(slot, new ItemStack(ICONS[slot].getMaterial()));
            }
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}

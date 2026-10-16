package com.ninjamcr.waystones.creation;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.models.Waystone;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.List;
import java.util.UUID;

public class WaystoneCreation {
    private final Waystones plugin;

    public WaystoneCreation(Waystones plugin) {
        this.plugin = plugin;
    }


    public void openWaystoneCreationDialog(Player player, Location loc) {

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
                                                    plugin.waystones.add(waystone);
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
}

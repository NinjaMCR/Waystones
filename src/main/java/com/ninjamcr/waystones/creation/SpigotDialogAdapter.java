package com.ninjamcr.waystones.creation;

import com.ninjamcr.waystones.Waystones;
import com.ninjamcr.waystones.listeners.SpigotDialogListener;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.dialog.ConfirmationDialog;
import net.md_5.bungee.api.dialog.DialogBase;
import net.md_5.bungee.api.dialog.action.ActionButton;
import net.md_5.bungee.api.dialog.action.CustomClickAction;
import net.md_5.bungee.api.dialog.input.TextInput;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;

// Class will only ever work on spigot compatible servers
public final class SpigotDialogAdapter implements WaystoneDialogAdapter {
    private final WaystoneCreation creation;
    private final NamespacedKey createAction;
    private final NamespacedKey cancelAction;

    public SpigotDialogAdapter(WaystoneCreation creation) {
        this.creation = creation;
        Waystones plugin = creation.getPlugin();
        this.createAction = new NamespacedKey(plugin, "create_waystone");
        this.cancelAction = new NamespacedKey(plugin, "cancel_waystone");
    }

    @Override
    public void registerHandlers(Waystones plugin) {
        Bukkit.getPluginManager().registerEvents(
                new SpigotDialogListener(creation, createAction, cancelAction), plugin
        );
    }

    @Override
    public void open(Player player) {
        TextInput input = new TextInput(
                WaystoneCreation.INPUT_NAME, new TextComponent("Waystone Name")
        ).width(300).labelVisible(true).initial("").maxLength(32);

        DialogBase base = new DialogBase(new TextComponent("Create Waystone"))
                .inputs(List.of(input));

        ActionButton create = new ActionButton(
                new TextComponent("Create"),
                new CustomClickAction(createAction.toString())
        );
        ActionButton cancel = new ActionButton(
                new TextComponent("Cancel"),
                new CustomClickAction(cancelAction.toString())
        );

        player.showDialog(new ConfirmationDialog(base, create, cancel));
    }
}

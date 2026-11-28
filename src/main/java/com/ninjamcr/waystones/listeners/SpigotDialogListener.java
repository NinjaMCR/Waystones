package com.ninjamcr.waystones.listeners;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ninjamcr.waystones.creation.WaystoneCreation;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCustomClickEvent;

public final class SpigotDialogListener implements Listener {
    private final WaystoneCreation creation;
    private final NamespacedKey chooseIconAction;
    private final NamespacedKey cancelAction;

    public SpigotDialogListener(WaystoneCreation creation, NamespacedKey chooseIconAction, NamespacedKey cancelAction) {
        this.creation = creation;
        this.chooseIconAction = chooseIconAction;
        this.cancelAction = cancelAction;
    }

    @EventHandler
    public void onDialogClick(PlayerCustomClickEvent event) {
        NamespacedKey action = event.getId();
        if (cancelAction.equals(action)) {
            creation.cancel(event.getPlayer());
            return;
        }
        if (!chooseIconAction.equals(action)) {
            return;
        }

        JsonElement data = event.getData();
        String name = null;
        String description = null;
        if (data != null && data.isJsonObject()) {
            JsonObject values = data.getAsJsonObject();
            JsonElement nameValue = values.get(WaystoneCreation.INPUT_NAME);
            if (nameValue != null && nameValue.isJsonPrimitive() && nameValue.getAsJsonPrimitive().isString()) {
                name = nameValue.getAsString();
            }

            JsonElement descValue = values.get(WaystoneCreation.INPUT_DESCRIPTION);
            if (descValue != null && descValue.isJsonPrimitive() && descValue.getAsJsonPrimitive().isString()) {
                description = descValue.getAsString();
            }
        }
        creation.chooseIcon(event.getPlayer(), name, description);
    }
}

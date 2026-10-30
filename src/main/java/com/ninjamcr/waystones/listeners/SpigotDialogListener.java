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
    private final NamespacedKey createAction;
    private final NamespacedKey cancelAction;

    public SpigotDialogListener(WaystoneCreation creation, NamespacedKey createAction, NamespacedKey cancelAction) {
        this.creation = creation;
        this.createAction = createAction;
        this.cancelAction = cancelAction;
    }

    @EventHandler
    public void onDialogClick(PlayerCustomClickEvent event) {
        NamespacedKey action = event.getId();
        if (cancelAction.equals(action)) {
            creation.cancel(event.getPlayer());
            return;
        }
        if (!createAction.equals(action)) {
            return;
        }

        JsonElement data = event.getData();
        String name = null;
        if (data != null && data.isJsonObject()) {
            JsonObject values = data.getAsJsonObject();
            JsonElement value = values.get(WaystoneCreation.INPUT_NAME);
            if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                name = value.getAsString();
            }
        }
        creation.confirm(event.getPlayer(), name);
    }
}

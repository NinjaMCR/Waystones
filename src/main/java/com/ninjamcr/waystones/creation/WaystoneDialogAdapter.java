package com.ninjamcr.waystones.creation;

import com.ninjamcr.waystones.Waystones;
import org.bukkit.entity.Player;

// A default interface for the different server types. Do not reference any kind of server type specific imports here (paper, bungeecord etc)
public interface WaystoneDialogAdapter {
    void open(Player player);

    default void registerHandlers(Waystones plugin) {
    }
}

package com.ninjamcr.waystones.creation;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;

import java.util.List;

// Class will only work on paper compatible servers
public final class PaperDialogAdapter implements WaystoneDialogAdapter {
    private final WaystoneCreation creation;

    public PaperDialogAdapter(WaystoneCreation creation) {
        this.creation = creation;
    }

    @Override
    public void open(Player player) {
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("Create Waystone"))
                        .inputs(List.of(DialogInput.text(WaystoneCreation.INPUT_NAME, Component.text("Waystone Name"))
                                .width(300)
                                .labelVisible(true)
                                .initial("")
                                .maxLength(32)
                                .build()))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("Create"))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player submittingPlayer) {
                                        creation.confirm(submittingPlayer, view.getText(WaystoneCreation.INPUT_NAME));
                                    }
                                }, ClickCallback.Options.builder().uses(1).build()))
                                .build(),
                        ActionButton.builder(Component.text("Cancel"))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player cancellingPlayer) {
                                        creation.cancel(cancellingPlayer);
                                    }
                                }, ClickCallback.Options.builder().uses(1).build()))
                                .build()
                )));

        ((Audience) player).showDialog(dialog);
    }
}

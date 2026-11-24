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
        ActionButton chooseIcon = ActionButton.builder(Component.text("Choose icon and create"))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player submittingPlayer) {
                        creation.chooseIcon(submittingPlayer, view.getText(WaystoneCreation.INPUT_NAME));
                    }
                }, ClickCallback.Options.builder().uses(1).build()))
                .build();
        ActionButton cancel = ActionButton.builder(Component.text("Cancel"))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player cancellingPlayer) {
                        creation.cancel(cancellingPlayer);
                    }
                }, ClickCallback.Options.builder().uses(1).build()))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("Create Waystone"))
                        .inputs(List.of(DialogInput.text(WaystoneCreation.INPUT_NAME, Component.text("Waystone Name"))
                                .width(300)
                                .labelVisible(true)
                                .initial("")
                                .maxLength(32)
                                .build()))
                        .build())
                .type(DialogType.confirmation(chooseIcon, cancel)));

        ((Audience) player).showDialog(dialog);
    }
}

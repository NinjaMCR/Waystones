
package com.ninjamcr.waystones;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.io.IOException;
import java.net.URISyntaxException;

public class WaystonesBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.DATAPACK_DISCOVERY,
                event -> {
                    try {
                        var resource = getClass()
                                .getResource("/waystones_datapack");

                        if (resource == null) {
                            throw new IllegalStateException("Waystones datapack folder not found");
                        }

                        event.registrar().discoverPack(resource.toURI(), "provided");
                    } catch (URISyntaxException | IOException e) {
                        throw new RuntimeException(
                                "Failed to register Waystones datapack", e
                        );
                    }
                }
        );
    }
}

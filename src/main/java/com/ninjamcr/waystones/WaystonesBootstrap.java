package com.ninjamcr.waystones;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

// Boostrap is only used by paper to load the waystones datapack
public final class WaystonesBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.DATAPACK_DISCOVERY,
                event -> {
                    URL resource = getClass().getResource("/waystones_datapack");
                    if (resource == null) {
                        throw new IllegalStateException( "Missing waystones_datapack resources in Waystones JAR");
                    }
                    try {
                        event.registrar().discoverPack(resource.toURI(), "provided", options -> options.autoEnableOnServerStart(true));
                    } catch (IOException | URISyntaxException e) {
                        throw new IllegalStateException(
                                "Could not register the bundled Waystones datapack", e);
                    }
                }
        );
    }
}

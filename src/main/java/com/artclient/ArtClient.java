package com.artclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public class ArtClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();
        ClientTickEvents.END_CLIENT_TICK.register(Tracker::tick);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("artclient", "hud"), Hud::render);
    }
}

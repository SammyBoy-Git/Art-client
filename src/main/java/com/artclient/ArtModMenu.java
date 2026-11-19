package com.artclient;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Lets Mod Menu open the Art Client settings from the mod's row. */
public class ArtModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ArtConfigScreen::new;
    }
}

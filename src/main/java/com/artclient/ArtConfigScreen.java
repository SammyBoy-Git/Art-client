package com.artclient;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/** Settings screen opened from Mod Menu: one on/off button per feature. */
public class ArtConfigScreen extends Screen {
    public ArtConfigScreen(Screen parent) {
        super(Component.literal("Art Client"));
    }

    static List<Field> booleanSettings() {
        List<Field> out = new ArrayList<>();
        for (Field f : Config.class.getFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && f.getType() == boolean.class) out.add(f);
        }
        return out;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 110, y = 40;
        for (Field f : booleanSettings()) {
            boolean value = getValue(f);
            Button b = Button.builder(Component.literal(prettyName(f.getName()) + ": " + (value ? "On" : "Off")),
                    btn -> {
                        setValue(f, !getValue(f));
                        Config.save();
                        this.clearWidgets();
                        this.init();
                    }).bounds(x, y, 220, 20).build();
            this.addRenderableWidget(b);
            y += 24;
        }
        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
                .bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void onClose() {
        // Closes back to the game. Returning to the Mod Menu list needs a screen-switch
        // call that I haven't confirmed for 26.3 yet.
        super.onClose();
    }

    static boolean getValue(Field f) {
        try { return f.getBoolean(Config.I); } catch (IllegalAccessException e) { return false; }
    }

    static void setValue(Field f, boolean v) {
        try { f.setBoolean(Config.I, v); } catch (IllegalAccessException e) { e.printStackTrace(); }
    }

    static String prettyName(String camel) {
        return camel.substring(0, 1).toUpperCase() + camel.substring(1).replaceAll("([A-Z])", " $1");
    }
}

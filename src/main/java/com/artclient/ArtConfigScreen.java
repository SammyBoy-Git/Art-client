package com.artclient;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.lang.reflect.Field;
import java.util.List;

/** Mod Menu settings screen: tabs at the top, Done always fixed at the bottom. */
public class ArtConfigScreen extends Screen {

    static final String[] TAB_NAMES = {"HUD", "Warnings", "Combat"};
    static final List<List<String>> TAB_SETTINGS = List.of(
            List.of("fps", "cps", "ping", "hunger", "reach", "damagePanel", "combo", "showTargetBlock"),
            List.of("lightWarning", "healthWarning", "hungerWarning", "skullWarning"),
            List.of("autoSprint")
    );
    static final java.util.Map<String, String> LABELS = new java.util.HashMap<>();
    static {
        LABELS.put("fps", "FPS");
        LABELS.put("cps", "CPS");
        LABELS.put("ping", "Ping");
        LABELS.put("hunger", "Food");
        LABELS.put("reach", "Reach");
        LABELS.put("damagePanel", "Damage line");
        LABELS.put("combo", "Combo");
        LABELS.put("showTargetBlock", "Block name");
        LABELS.put("lightWarning", "Low light icon");
        LABELS.put("healthWarning", "Low health icon");
        LABELS.put("hungerWarning", "Low food icon");
        LABELS.put("skullWarning", "Threat skull icon");
        LABELS.put("autoSprint", "Auto sprint");
    }

    private int tab = 0;

    public ArtConfigScreen(Screen parent) {
        super(Component.literal("Art Client"));
    }

    @Override
    protected void init() {
        // tab buttons across the top
        int tabW = 90, gap = 4;
        int startX = this.width / 2 - (TAB_NAMES.length * (tabW + gap) - gap) / 2;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int idx = i;
            String label = (i == tab ? "> " : "") + TAB_NAMES[i];
            this.addRenderableWidget(Button.builder(Component.literal(label), btn -> switchTab(idx))
                    .bounds(startX + i * (tabW + gap), 30, tabW, 20).build());
        }

        // the settings for the selected tab, one per row, below the tabs
        int x = this.width / 2 - 110, y = 60;
        for (String name : TAB_SETTINGS.get(tab)) {
            Field f = field(name);
            if (f == null) continue;
            boolean value = getValue(f);
            this.addRenderableWidget(Button.builder(
                    Component.literal(LABELS.getOrDefault(name, name) + ": " + (value ? "On" : "Off")),
                    btn -> {
                        setValue(f, !getValue(f));
                        Config.save();
                        this.clearWidgets();
                        this.init();
                    }).bounds(x, y, 220, 20).build());
            y += 24;
        }

        // Done is placed last and stays at the bottom, so tabs never cover it
        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    void switchTab(int idx) {
        tab = idx;
        this.clearWidgets();
        this.init();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    static Field field(String name) {
        try {
            Field f = Config.class.getField(name);
            return f.getType() == boolean.class ? f : null;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    static boolean getValue(Field f) {
        try { return f.getBoolean(Config.I); } catch (IllegalAccessException e) { return false; }
    }

    static void setValue(Field f, boolean v) {
        try { f.setBoolean(Config.I, v); } catch (IllegalAccessException e) { e.printStackTrace(); }
    }
}

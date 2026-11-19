package com.artclient;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.ArrayList;
import java.util.List;

/** Draws the custom crosshair and manages saved presets. */
public class Crosshair {

    public static void render(GuiGraphicsExtractor g, int screenW, int screenH, int[] shake) {
        Config c = Config.I;
        if (c.crosshairStyle == null || c.crosshairStyle.equals("off")) return;
        int cx = screenW / 2 + shake[0];
        int cy = screenH / 2 + shake[1];
        int col = colorOf(c.crosshairColor);
        int size = Math.max(1, c.crosshairSize), gap = Math.max(0, c.crosshairGap), t = Math.max(1, c.crosshairThickness);

        switch (c.crosshairStyle) {
            case "dot" -> g.fill(cx - t, cy - t, cx + t, cy + t, col);
            case "square" -> {
                int s = size;
                g.fill(cx - s, cy - s, cx + s, cy - s + t, col);
                g.fill(cx - s, cy + s - t, cx + s, cy + s, col);
                g.fill(cx - s, cy - s, cx - s + t, cy + s, col);
                g.fill(cx + s - t, cy - s, cx + s, cy + s, col);
            }
            default -> { // cross
                g.fill(cx - gap - size, cy - t / 2, cx - gap, cy + t / 2 + 1, col);
                g.fill(cx + gap, cy - t / 2, cx + gap + size, cy + t / 2 + 1, col);
                g.fill(cx - t / 2, cy - gap - size, cx + t / 2 + 1, cy - gap, col);
                g.fill(cx - t / 2, cy + gap, cx + t / 2 + 1, cy + gap + size, col);
            }
        }
    }

    static int colorOf(String hex) {
        try { return 0xFF000000 | Integer.parseInt(hex.replace("#", ""), 16); }
        catch (Exception e) { return 0xFFFFFFFF; }
    }

    /** Save the current crosshair under a name, replacing any old preset with that name. */
    public static void savePreset(String name) {
        Config c = Config.I;
        c.savedCrosshairs.removeIf(s -> s.startsWith(name + "|"));
        c.savedCrosshairs.add(name + "|" + c.crosshairStyle + "|" + c.crosshairColor + "|"
                + c.crosshairSize + "|" + c.crosshairGap + "|" + c.crosshairThickness);
        Config.save();
    }

    /** Load a saved crosshair by name into the current settings. Returns false if not found. */
    public static boolean loadPreset(String name) {
        for (String s : Config.I.savedCrosshairs) {
            String[] p = s.split("\\|");
            if (p.length == 6 && p[0].equals(name)) {
                Config c = Config.I;
                c.crosshairStyle = p[1];
                c.crosshairColor = p[2];
                c.crosshairSize = Integer.parseInt(p[3]);
                c.crosshairGap = Integer.parseInt(p[4]);
                c.crosshairThickness = Integer.parseInt(p[5]);
                Config.save();
                return true;
            }
        }
        return false;
    }

    public static List<String> presetNames() {
        List<String> out = new ArrayList<>();
        for (String s : Config.I.savedCrosshairs) out.add(s.split("\\|")[0]);
        return out;
    }
}

package com.artclient;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.LivingEntity;
import java.util.ArrayList;
import java.util.List;

public class Hud {
    static final long HIGHLIGHT_MS = 5000;

    public static void render(GuiGraphicsExtractor g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        Tracker.pollClicks(mc);
        Config c = Config.I;
        Font f = mc.font;
        long now = System.currentTimeMillis();
        int w = g.guiWidth();

        // ===== small left panel: only the important stats =====
        List<String> L = new ArrayList<>();
        if (c.fps) L.add("FPS " + mc.getFps());
        if (c.cps) L.add("CPS " + Tracker.cps(Tracker.left) + " | " + Tracker.cps(Tracker.right));
        if (c.ping) {
            var con = mc.getConnection();
            PlayerInfo pi = con == null ? null : con.getPlayerInfo(mc.player.getUUID());
            if (pi != null) L.add("PING " + pi.getLatency());
        }
        if (c.hunger) L.add("FOOD " + mc.player.getFoodData().getFoodLevel());
        if (c.reach && Tracker.lastReach > 0) L.add(String.format("REACH %.2f", Tracker.lastReach));
        drawBox(g, f, L, 4, 4, false);
        drawTargetCrosshair(g, mc);

        // ===== top-center: one compact damage line =====
        if (c.damagePanel) {
            boolean hit = now - Tracker.lastHitAt < 3000, taken = now - Tracker.lastTakenAt < 3000;
            String line = "DMG " + (hit ? "+" + fmt(Tracker.lastHit) : "0")
                    + "   TAKEN " + (taken ? "-" + fmt(Tracker.lastTaken) : "0")
                    + "   TOTAL " + fmt(Tracker.totalDealt);
            if (c.combo && Tracker.combo > 0) line += "   COMBO " + Tracker.combo;
            drawBox(g, f, List.of(line), (w - f.width(line)) / 2 - 3, 4, true);
        }

    }

    // red crosshair drawn over the center when aiming at a mob or player
    static void drawTargetCrosshair(GuiGraphicsExtractor g, Minecraft mc) {
        if (!Config.I.redCrosshairOnTarget) return;
        var hit = mc.crosshairPickEntity;
        if (!(hit instanceof net.minecraft.world.entity.LivingEntity) || hit == mc.player) return;
        int cx = g.guiWidth() / 2, cy = g.guiHeight() / 2;
        int red = 0xFFFF3030;
        g.fill(cx - 6, cy - 1, cx + 7, cy + 1, red);
        g.fill(cx - 1, cy - 6, cx + 1, cy + 7, red);
    }

    static String fmt(float v) { return v == (int) v ? String.valueOf((int) v) : String.format("%.1f", v); }

    static void drawBox(GuiGraphicsExtractor g, Font f, List<String> lines, int x, int y, boolean center) {
        if (lines.isEmpty()) return;
        int pw = 0;
        for (String s : lines) pw = Math.max(pw, f.width(s));
        g.fill(x - 2, y - 2, x + pw + 2, y + lines.size() * 9 + 1, 0x66000000);
        int yy = y;
        for (String s : lines) {
            g.text(f, s, x, yy, Config.textCol(), true);
            yy += 9;
        }
    }
}

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
        drawWarnings(g, mc);
        drawTargetBlock(g, mc);

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

    // ===== top-right warning icons: light and heart, both red =====
    static final int RED = 0xFFFF3030;
    static final int[][] HEART = {
        {0,1,1,0,1,1,0},
        {1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1},
        {0,1,1,1,1,1,0},
        {0,0,1,1,1,0,0},
        {0,0,0,1,0,0,0}
    };
    static final int[][] SUN = {
        {0,0,0,1,0,0,0},
        {0,1,0,1,0,1,0},
        {0,0,1,1,1,0,0},
        {1,1,1,1,1,1,1},
        {0,0,1,1,1,0,0},
        {0,1,0,1,0,1,0},
        {0,0,0,1,0,0,0}
    };

    static void drawIcon(GuiGraphicsExtractor g, int[][] pattern, int x, int y, int scale) {
        for (int r = 0; r < pattern.length; r++)
            for (int col = 0; col < pattern[r].length; col++)
                if (pattern[r][col] == 1)
                    g.fill(x + col * scale, y + r * scale, x + (col + 1) * scale, y + (r + 1) * scale, RED);
    }

    static final int[][] HUNGER = {
        {0,1,1,1,0,0,0},
        {1,1,1,1,1,0,0},
        {1,1,1,1,1,0,0},
        {0,1,1,1,1,0,1},
        {0,0,1,1,0,1,1},
        {0,0,0,0,1,1,1}
    };

    static final int[][] SKULL = {
        {0,1,1,1,1,1,0},
        {1,1,1,1,1,1,1},
        {1,1,0,1,0,1,1},
        {1,1,1,1,1,1,1},
        {0,1,0,1,0,1,0},
        {0,1,1,1,1,1,0}
    };

    /** True if a hostile mob or another player is within the given range. */
    static boolean threatNearby(Minecraft mc, double range) {
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || !(e instanceof net.minecraft.world.entity.LivingEntity le) || !le.isAlive()) continue;
            if (!(e instanceof net.minecraft.world.entity.monster.Monster) && !(e instanceof net.minecraft.world.entity.player.Player)) continue;
            if (e.distanceTo(mc.player) <= range) return true;
        }
        return false;
    }

    static void drawWarnings(GuiGraphicsExtractor g, Minecraft mc) {
        Config c = Config.I;
        int w = g.guiWidth();
        int scale = 2, iconW = 7 * scale;
        int x = w - iconW - 6, y = 6;
        if (c.lightWarning) {
            int light = mc.level.getMaxLocalRawBrightness(mc.player.blockPosition());
            if (light <= c.lowLightLevel) {
                drawIcon(g, SUN, x, y, scale);
                y += 8 * scale + 4;
            }
        }
        if (c.healthWarning && mc.player.getHealth() <= c.lowHealthAmount) {
            drawIcon(g, HEART, x, y, scale);
            y += 6 * scale + 4;
        }
        if (c.hungerWarning && mc.player.getFoodData().getFoodLevel() <= c.lowHungerAmount) {
            drawIcon(g, HUNGER, x, y, scale);
            y += 6 * scale + 4;
        }
        if (c.skullWarning && threatNearby(mc, c.skullRangeBlocks)) {
            drawIcon(g, SKULL, x, y, scale);
        }
    }

    // block name in the bottom-right corner, for the block under the crosshair
    static void drawTargetBlock(GuiGraphicsExtractor g, Minecraft mc) {
        if (!Config.I.showTargetBlock) return;
        if (!(mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult bhr)) return;
        var state = mc.level.getBlockState(bhr.getBlockPos());
        if (state.isAir()) return;
        String name = state.getBlock().getName().getString();
        int x = g.guiWidth() - mc.font.width(name) - 6;
        int y = g.guiHeight() - 14;
        g.fill(x - 3, y - 3, x + mc.font.width(name) + 3, y + 10, 0x66000000);
        g.text(mc.font, name, x, y, Config.textCol(), true);
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

package com.artclient;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Hud {
    static final DateTimeFormatter CLK = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void render(GuiGraphicsExtractor g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        Tracker.pollClicks(mc);
        Config c = Config.I; Font f = mc.font;
        long now = System.currentTimeMillis();
        int w = g.guiWidth(), h = g.guiHeight();
        boolean R = c.panelsOnRight;

        // =========== MAIN INFO PANEL ===========
        List<String> L = new ArrayList<>();
        if (c.fps) L.add("FPS: " + mc.getFps());
        if (c.cps) L.add("CPS: " + Tracker.cps(Tracker.left) + " | " + Tracker.cps(Tracker.right));
        if (c.ping) {
            var con = mc.getConnection(); PlayerInfo pi = con == null ? null : con.getPlayerInfo(mc.player.getUUID());
            if (pi != null) L.add("Ping: " + pi.getLatency() + " ms");
        }
        if (c.serverIp) { var sd = mc.getCurrentServer(); L.add("Server: " + (sd != null ? sd.ip : "Singleplayer")); }
        if (c.coords) L.add(String.format("XYZ: %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        if (c.facing) L.add("Facing: " + mc.player.getDirection());
        if (c.speed) L.add(String.format("Speed: %.2f b/s", Tracker.speed));
        if (c.worldTime) {
            long t = mc.level.getDayTime(); long tod = t % 24000;
            L.add(String.format("Day %d  %02d:%02d", t / 24000 + 1, (tod / 1000 + 6) % 24, (tod % 1000) * 60 / 1000));
        }
        if (c.clock) L.add("Time: " + LocalTime.now().format(CLK));
        if (c.memory) { Runtime r = Runtime.getRuntime(); L.add("RAM: " + ((r.totalMemory() - r.freeMemory()) >> 20) + "/" + (r.maxMemory() >> 20) + " MB"); }
        if (c.hunger) L.add("Food: " + mc.player.getFoodData().getFoodLevel() + "/20");
        if (c.combo && Tracker.combo > 0) L.add("Combo: " + Tracker.combo);
        if (c.kills) L.add("Kills: " + Tracker.kills);
        if (c.reach && Tracker.lastReach > 0) L.add(String.format("Reach: %.2f", Tracker.lastReach));
        if (c.armor) {
            StringBuilder sb = new StringBuilder("Armor:");
            for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack st = mc.player.getItemBySlot(s);
                sb.append(' ').append(st.isDamageableItem() ? st.getMaxDamage() - st.getDamageValue() : "-");
            }
            L.add(sb.toString());
            ItemStack held = mc.player.getMainHandItem();
            if (held.isDamageableItem()) L.add("Held: " + (held.getMaxDamage() - held.getDamageValue()) + "/" + held.getMaxDamage());
        }
        int y = 4;
        if (c.watermark) {
            int ww = f.width(c.watermarkText);
            int wx = R ? w - ww - 4 : 4;
            g.fill(wx - 3, y - 3, wx + ww + 3, y + 10, Config.bg());
            g.text(f, c.watermarkText, wx, y, Config.accentColor(), true);
            y += 15;
        }
        panel(g, f, L, R, w, y);

        // =========== OPPOSITE-SIDE PANEL: potion effects + item counts ===========
        List<String> E = new ArrayList<>();
        if (c.effects) {
            for (MobEffectInstance e : mc.player.getActiveEffects()) {
                String name = e.getEffect().value().getDisplayName().getString();
                int amp = e.getAmplifier() + 1;
                String tm = e.isInfiniteDuration() ? "inf" : String.format("%d:%02d", e.getDuration() / 1200, (e.getDuration() / 20) % 60);
                E.add(name + (amp > 1 ? " " + amp : "") + "  " + tm);
            }
        }
        if (c.items) {
            int totems = 0, gapples = 0, arrows = 0, pearls = 0;
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                ItemStack s = mc.player.getInventory().getItem(i);
                if (s.isEmpty()) continue;
                if (s.is(Items.TOTEM_OF_UNDYING)) totems += s.getCount();
                else if (s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE)) gapples += s.getCount();
                else if (s.is(Items.ARROW)) arrows += s.getCount();
                else if (s.is(Items.ENDER_PEARL)) pearls += s.getCount();
            }
            if (totems > 0) E.add("Totems: " + totems);
            if (gapples > 0) E.add("Gapples: " + gapples);
            if (arrows > 0) E.add("Arrows: " + arrows);
            if (pearls > 0) E.add("Pearls: " + pearls);
        }
        panel(g, f, E, !R, w, 4);

        // =========== TOP-CENTER DAMAGE PANEL ===========
        if (c.damagePanel) {
            List<String> D = new ArrayList<>();
            boolean hit = now - Tracker.lastHitAt < 3000, taken = now - Tracker.lastTakenAt < 3000;
            D.add(String.format("Dealt: %s  (total %.1f)", hit ? "+" + fmt(Tracker.lastHit) : "-", Tracker.totalDealt));
            D.add(String.format("Taken: %s  (total %.1f)", taken ? "-" + fmt(Tracker.lastTaken) : "-", Tracker.totalTaken));
            int pw = 0; for (String s : D) pw = Math.max(pw, f.width(s));
            drawBox(g, f, D, (w - pw) / 2, 4);
        }

        // =========== MOB HEALTH BAR ===========
        LivingEntity t = Tracker.target;
        if (c.mobHealth && t != null && t.isAlive() && now - Tracker.targetSeenAt < 1500) {
            String name = t.getName().getString();
            float hp = t.getHealth(), max = Math.max(1f, t.getMaxHealth());
            String txt = name + "  " + fmt(hp) + " / " + fmt(max);
            int bw = Math.max(120, f.width(txt) + 8), x = (w - bw) / 2, yy = 36;
            g.fill(x - 2, yy - 2, x + bw + 2, yy + 24, Config.bg());
            g.text(f, txt, x + (bw - f.width(txt)) / 2, yy, Config.textCol(), true);
            float p = Math.min(1f, hp / max);
            int col = p > .6f ? 0xFF3DDC4A : p > .3f ? 0xFFE8C030 : 0xFFE04040;
            g.fill(x, yy + 12, x + bw, yy + 20, 0xFF222222);
            g.fill(x, yy + 12, x + (int) (bw * p), yy + 20, col);
        }

        // =========== KEYSTROKES ===========
        if (c.keystrokes) {
            var o = mc.options; int kx = R ? w - 70 : 4, ky = h - 96;
            key(g, f, "W", kx + 22, ky, 20, 20, o.keyUp.isDown());
            key(g, f, "A", kx, ky + 22, 20, 20, o.keyLeft.isDown());
            key(g, f, "S", kx + 22, ky + 22, 20, 20, o.keyDown.isDown());
            key(g, f, "D", kx + 44, ky + 22, 20, 20, o.keyRight.isDown());
            key(g, f, "SPACE", kx, ky + 44, 64, 14, o.keyJump.isDown());
            key(g, f, "LMB " + Tracker.cps(Tracker.left), kx, ky + 60, 31, 14, Tracker.leftDown);
            key(g, f, "RMB " + Tracker.cps(Tracker.right), kx + 33, ky + 60, 31, 14, Tracker.rightDown);
        }
    }

    static String fmt(float v) { return v == (int) v ? String.valueOf((int) v) : String.format("%.1f", v); }

    static void key(GuiGraphicsExtractor g, Font f, String label, int x, int y, int w, int h, boolean down) {
        g.fill(x, y, x + w, y + h, down ? 0xCCFFFFFF : Config.bg() | 0x00101010);
        g.text(f, label, x + (w - f.width(label)) / 2, y + (h - 8) / 2, down ? 0xFF000000 : Config.textCol(), false);
    }

    static void drawBox(GuiGraphicsExtractor g, Font f, List<String> lines, int x, int y) {
        if (lines.isEmpty()) return;
        int pw = 0; for (String s : lines) pw = Math.max(pw, f.width(s));
        g.fill(x - 3, y - 3, x + pw + 3, y + lines.size() * 11 + 1, Config.bg());
        int yy = y; for (String s : lines) { g.text(f, s, x, yy, Config.textCol(), true); yy += 11; }
    }

    static void panel(GuiGraphicsExtractor g, Font f, List<String> lines, boolean right, int screenW, int y) {
        if (lines.isEmpty()) return;
        int pw = 0; for (String s : lines) pw = Math.max(pw, f.width(s));
        drawBox(g, f, lines, right ? screenW - pw - 4 : 4, y);
    }
}

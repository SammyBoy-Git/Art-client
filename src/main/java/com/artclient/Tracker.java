package com.artclient;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.*;

/** Collects clicks, damage dealt/taken, kills, combo, reach, target info and speed. */
public class Tracker {
    static final ArrayDeque<Long> left = new ArrayDeque<>(), right = new ArrayDeque<>();
    static boolean leftDown, rightDown;

    public static float lastHit, lastTaken, totalDealt, totalTaken;
    public static long lastHitAt, lastTakenAt;
    public static int combo, kills;
    public static double lastReach;
    public static LivingEntity target; public static long targetSeenAt;
    public static LivingEntity hitTarget; public static long hitAt;
    public static double speed;

    static float lastHp = -1; static double lx, lz; static int lastAttackId = -1; static long lastAttackAt;
    static final Map<Integer, Float> hp = new HashMap<>();

    /** Called every rendered frame from the HUD so fast clicks aren't missed. */
    public static void pollClicks(Minecraft mc) {
        long now = System.currentTimeMillis();
        boolean l = mc.options.keyAttack.isDown();
        boolean r = mc.options.keyUse.isDown();
        if (l && !leftDown) {
            left.add(now);
            if (target != null && target.isAlive() && now - targetSeenAt < 300 && mc.player != null) {
                lastAttackId = target.getId(); lastAttackAt = now;
                // approximate reach = distance from your eyes to the nearest point of the target's hitbox
                lastReach = Math.sqrt(target.getBoundingBox().distanceToSqr(mc.player.getEyePosition()));
            }
        }
        if (r && !rightDown) right.add(now);
        leftDown = l; rightDown = r;
    }
    public static boolean leftDownState() { return leftDown; }
    public static boolean rightDownState() { return rightDown; }

    public static int cps(ArrayDeque<Long> q) {
        long now = System.currentTimeMillis();
        while (!q.isEmpty() && now - q.peekFirst() > Config.I.cpsWindowMs) q.pollFirst();
        return q.size();
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null) { lastHp = -1; hp.clear(); return; }
        long now = System.currentTimeMillis();

        // optional auto-sprint while holding forward
        if (Config.I.autoSprint && mc.options.keyUp.isDown()) mc.options.keySprint.setDown(true);

        // damage taken (health + absorption) - also resets your combo
        float cur = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (lastHp >= 0 && cur < lastHp) { lastTaken = lastHp - cur; totalTaken += lastTaken; lastTakenAt = now; combo = 0; }
        lastHp = cur;

        // crosshair target
        Entity t = mc.crosshairPickEntity;
        if (t instanceof LivingEntity le && le != mc.player) { target = le; targetSeenAt = now; }

        // damage dealt / combo / kills: health drop on the entity you attacked in the last second
        Set<Integer> seen = new HashSet<>();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity le) || le == mc.player || le.distanceTo(mc.player) > 16) continue;
            int id = le.getId(); seen.add(id);
            float h = le.getHealth(), old = hp.getOrDefault(id, h);
            if (h < old && id == lastAttackId && now - lastAttackAt < 1000) {
                lastHit = old - h; totalDealt += lastHit; lastHitAt = now; combo++; hitTarget = le; hitAt = now;
                if (h <= 0) kills++;
            }
            hp.put(id, h);
        }
        hp.keySet().retainAll(seen);
        if (now - lastHitAt > 4000) combo = 0;

        // speed (blocks/sec)
        speed = Math.hypot(mc.player.getX() - lx, mc.player.getZ() - lz) * 20;
        lx = mc.player.getX(); lz = mc.player.getZ();
    }
}

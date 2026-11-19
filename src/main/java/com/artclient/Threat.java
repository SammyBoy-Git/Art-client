package com.artclient;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

/** Finds how close the nearest hostile mob or other player is, as a 0..1 intensity. */
public class Threat {
    public static float intensity(Minecraft mc) {
        if (!Config.I.shakeEnabled || mc.player == null || mc.level == null) return 0f;
        double range = Math.max(1, Config.I.shakeRangeBlocks);
        double best = range;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || !(e instanceof LivingEntity le) || !le.isAlive()) continue;
            if (!(e instanceof Monster) && !(e instanceof Player)) continue;
            double d = e.distanceTo(mc.player);
            if (d < best) best = d;
        }
        return best >= range ? 0f : (float) ((range - best) / range);
    }

    /** Pixel offset for the crosshair: stronger the closer the threat is. */
    public static int[] shakeOffset(float intensity) {
        if (intensity <= 0f) return new int[]{0, 0};
        double t = System.currentTimeMillis() / 30.0;
        int amp = Math.round(1 + intensity * 6);
        return new int[]{(int) Math.round(Math.sin(t * 1.7) * amp), (int) Math.round(Math.cos(t * 2.3) * amp)};
    }
}

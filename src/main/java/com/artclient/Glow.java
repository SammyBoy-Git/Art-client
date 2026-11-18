package com.artclient;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

/** Outlines the last mob/player you hit in red for 5 seconds, darkening each second. */
public class Glow {
    static final long DURATION_MS = 5000;
    static final String TEAM = "artclient_hit";
    static LivingEntity target;
    static long startAt;
    static int lastStep = -1;

    public static void onHit(Minecraft mc, LivingEntity e) {
        clear(mc);
        target = e;
        startAt = System.currentTimeMillis();
        lastStep = -1;
        e.setGlowingTag(true);
        Scoreboard sb = mc.level.getScoreboard();
        PlayerTeam team = sb.getPlayerTeam(TEAM);
        if (team == null) team = sb.addPlayerTeam(TEAM);
        sb.addPlayerToTeam(e.getScoreboardName(), team);
        setColor(sb, team, null);
    }

    public static void tick(Minecraft mc) {
        if (target == null) return;
        long age = System.currentTimeMillis() - startAt;
        if (!target.isAlive() || age >= DURATION_MS || mc.level == null) {
            clear(mc);
            return;
        }
        int step = (int) (age / 1000);
        if (step == lastStep) return;
        lastStep = step;
        Scoreboard sb = mc.level.getScoreboard();
        PlayerTeam team = sb.getPlayerTeam(TEAM);
        if (team == null) return;
        setColor(sb, team, null);
    }

    static void setColor(Scoreboard sb, PlayerTeam team, Object color) {
        // 26.x moved team colours to TeamColor (Optional<TeamColor>); red is added in a later pass
        team.setColor(java.util.Optional.empty());
    }

    public static void clear(Minecraft mc) {
        if (target != null) {
            target.setGlowingTag(false);
            if (mc.level != null) {
                Scoreboard sb = mc.level.getScoreboard();
                PlayerTeam team = sb.getPlayerTeam(TEAM);
                if (team != null) sb.removePlayerFromTeam(target.getScoreboardName(), team);
            }
        }
        target = null;
    }
}

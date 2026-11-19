package com.artclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;

/** Settings live in config/art_client.json, and can also be changed from Mod Menu. */
public class Config {
    // ---- info panel
    public boolean watermark = false, fps = true, cps = true, ping = true, coords = false, facing = false,
            speed = false, memory = false, clock = false, worldTime = false, serverIp = false,
            hunger = true, armor = false;
    // ---- combat info
    public boolean mobHealth = true, damagePanel = true, combo = true, kills = false, reach = true;
    // ---- extras
    public boolean keystrokes = false, effects = false, items = false;
    public boolean autoSprint = false;
    // ---- personal style
    public String accent = "#55FFFF";
    public String textColor = "#FFFFFF";
    public int backgroundAlpha = 102;
    public boolean panelsOnRight = false;
    public String watermarkText = "Art Client";
    public int cpsWindowMs = 1000;
    // draw a red crosshair when aiming at a mob or player
    public boolean redCrosshairOnTarget = true;

    public static Config I = new Config();
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("art_client.json"); }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) I = GSON.fromJson(Files.readString(p), Config.class);
            save();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void save() {
        try { Files.writeString(path(), GSON.toJson(I)); } catch (Exception e) { e.printStackTrace(); }
    }

    static int parse(String hex, int fallback) {
        try { return 0xFF000000 | Integer.parseInt(hex.replace("#", ""), 16); } catch (Exception e) { return fallback; }
    }
    public static int accentColor() { return parse(I.accent, 0xFF55FFFF); }
    public static int textCol()     { return parse(I.textColor, 0xFFFFFFFF); }
    public static int bg()          { return (Math.max(0, Math.min(255, I.backgroundAlpha)) << 24); }
}

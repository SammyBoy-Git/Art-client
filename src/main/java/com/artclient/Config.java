package com.artclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;

/** Edit .minecraft/config/art_client.json (in your Modrinth instance folder) to make it yours. */
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
    public String accent = "#55FFFF";      // watermark / bar highlight colour
    public String textColor = "#FFFFFF";
    public int backgroundAlpha = 102;      // 0 = no background, 255 = solid
    public boolean panelsOnRight = false;  // flip the whole layout
    public String watermarkText = "Art Client";
    public int cpsWindowMs = 1000;

    public static Config I = new Config();
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("art_client.json"); }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) I = GSON.fromJson(Files.readString(p), Config.class);
            Files.writeString(p, GSON.toJson(I));   // writes defaults / any new options
        } catch (Exception e) { e.printStackTrace(); }
    }

    static int parse(String hex, int fallback) {
        try { return 0xFF000000 | Integer.parseInt(hex.replace("#", ""), 16); } catch (Exception e) { return fallback; }
    }
    public static int accentColor() { return parse(I.accent, 0xFF55FFFF); }
    public static int textCol()     { return parse(I.textColor, 0xFFFFFFFF); }
    public static int bg()          { return (Math.max(0, Math.min(255, I.backgroundAlpha)) << 24); }
}

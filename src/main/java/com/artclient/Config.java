package com.artclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;

/** Settings live in config/art_client.json, and can also be changed from Mod Menu. */
public class Config {
    // ---- HUD tab: on/off
    public boolean fps = true, cps = true, ping = true, hunger = true, reach = true,
            damagePanel = true, combo = true, showTargetBlock = true;

    // ---- Warnings tab: on/off
    public boolean lightWarning = true, healthWarning = true, hungerWarning = true, skullWarning = true;

    // ---- Combat tab: on/off
    public boolean autoSprint = false;

    // ---- thresholds (edit in art_client.json)
    public int lowLightLevel = 7;       // light icon at or below this light level
    public int lowHealthAmount = 6;     // heart icon at or below this health
    public int lowHungerAmount = 6;     // drumstick at or below this food level
    public int skullRangeBlocks = 16;   // skull when a hostile or player is this close
    public int cpsWindowMs = 1000;

    // ---- style
    public String textColor = "#FFFFFF";

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
    public static int textCol() { return parse(I.textColor, 0xFFFFFFFF); }
}

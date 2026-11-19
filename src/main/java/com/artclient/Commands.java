package com.artclient;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/** Handles client-side "/Art ..." commands. Nothing is sent to the server. */
public class Commands {

    /** Returns false to block the command from reaching the server. */
    public static boolean handle(String command) {
        if (!command.startsWith("Art") ) return true;
        if (command.length() > 3 && command.charAt(3) != ' ') return true;
        String[] a = command.trim().split("\\s+");
        try {
            run(a);
        } catch (Exception e) {
            say("Error: " + e.getMessage() + "  (type /Art help)");
        }
        return false;
    }

    static void run(String[] a) throws Exception {
        if (a.length < 2 || a[1].equalsIgnoreCase("help")) { help(); return; }
        Config c = Config.I;
        switch (a[1].toLowerCase()) {
            case "crosshair" -> {
                if (a.length < 3) { say("Usage: /Art crosshair <style|color|size|gap|thickness|save|load|list> ..."); return; }
                switch (a[2].toLowerCase()) {
                    case "style" -> {
                        String s = a[3].toLowerCase();
                        if (!Arrays.asList("cross", "dot", "square", "off").contains(s)) { say("Styles: cross, dot, square, off"); return; }
                        c.crosshairStyle = s;
                    }
                    case "color" -> c.crosshairColor = a[3].startsWith("#") ? a[3] : "#" + a[3];
                    case "size" -> c.crosshairSize = Integer.parseInt(a[3]);
                    case "gap" -> c.crosshairGap = Integer.parseInt(a[3]);
                    case "thickness" -> c.crosshairThickness = Integer.parseInt(a[3]);
                    case "save" -> { Crosshair.savePreset(a[3]); say("Saved crosshair '" + a[3] + "'"); return; }
                    case "load" -> { say(Crosshair.loadPreset(a[3]) ? "Loaded '" + a[3] + "'" : "No saved crosshair named '" + a[3] + "'"); return; }
                    case "list" -> { say("Saved: " + String.join(", ", Crosshair.presetNames())); return; }
                    default -> { say("Unknown crosshair option: " + a[2]); return; }
                }
                Config.save();
                say("Crosshair " + a[2] + " set to " + a[3]);
            }
            case "set" -> {
                if (a.length < 4) { say("Usage: /Art set <setting> <value>"); return; }
                Field f = field(a[2]);
                f.set(c, parse(f, a[3]));
                Config.save();
                say(a[2] + " = " + f.get(c));
            }
            case "get" -> {
                if (a.length < 3) { say("Usage: /Art get <setting>"); return; }
                Field f = field(a[2]);
                say(a[2] + " = " + f.get(c));
            }
            case "toggle" -> {
                if (a.length < 3) { say("Usage: /Art toggle <setting>"); return; }
                Field f = field(a[2]);
                if (f.getType() != boolean.class) { say(a[2] + " is not on/off"); return; }
                boolean now = !f.getBoolean(c);
                f.setBoolean(c, now);
                Config.save();
                say(a[2] + " = " + now);
            }
            case "list" -> {
                StringBuilder sb = new StringBuilder("Settings: ");
                for (Field f : Config.class.getFields()) {
                    if (Modifier.isStatic(f.getModifiers())) continue;
                    sb.append(f.getName()).append(", ");
                }
                say(sb.toString());
            }
            default -> say("Unknown command. Type /Art help");
        }
    }

    static Field field(String name) throws NoSuchFieldException {
        Field f = Config.class.getField(name);
        if (Modifier.isStatic(f.getModifiers())) throw new NoSuchFieldException(name);
        return f;
    }

    static Object parse(Field f, String v) {
        Class<?> t = f.getType();
        if (t == boolean.class) return Boolean.parseBoolean(v);
        if (t == int.class) return Integer.parseInt(v);
        if (t == String.class) return v;
        throw new IllegalArgumentException("can't set " + f.getName() + " from chat");
    }

    static void help() {
        say("/Art crosshair style <cross|dot|square|off>");
        say("/Art crosshair color <#hex>  |  size <n>  |  gap <n>  |  thickness <n>");
        say("/Art crosshair save <name>  |  load <name>  |  list");
        say("/Art set <setting> <value>  |  get <setting>  |  toggle <setting>  |  list");
    }

    static void say(String msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.gui.chat.addClientSystemMessage(Component.literal("[Art] " + msg));
    }
}

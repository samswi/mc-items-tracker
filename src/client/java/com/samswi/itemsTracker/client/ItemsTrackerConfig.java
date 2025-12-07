package com.samswi.itemsTracker.client;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ItemsTrackerConfig {
    public static float HUD_SCALE = 1;
    public static int HUD_OFFSET_X = 10;
    public static int HUD_OFFSET_Y = 10;
    public static float HUD_BG_OPACITY = 0.5F;

    public static HudPositionsX HUD_POSITION_X = HudPositionsX.LEFT;

    public enum HudPositionsX{
        LEFT,
        RIGHT,
    }

    public static void saveConfig(){
        try (FileWriter writer = new FileWriter(ItemsTrackerClient.configFile)){
            writer.write("HUD_SCALE=" + HUD_SCALE + "\n" +
                    "HUD_OFFSET_X=" + HUD_OFFSET_X + "\n" +
                    "HUD_OFFSET_Y=" + HUD_OFFSET_Y + "\n" +
                    "HUD_POSITION_X=" + HUD_POSITION_X + "\n" +
                    "HUD_BG_OPACITY=" + HUD_BG_OPACITY);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadConfig(){
        try {
            if (!ItemsTrackerClient.configFile.exists()) return;
            Scanner scanner = new Scanner(ItemsTrackerClient.configFile);
            Map<String, String> values = new HashMap<>();
            while (scanner.hasNextLine()) {
                String[] strings = scanner.nextLine().split("=");
                values.put(strings[0], strings[1]);
            }
            HUD_SCALE = Float.parseFloat(values.getOrDefault("HUD_SCALE", "1"));
            HUD_OFFSET_X = Integer.parseInt(values.getOrDefault("HUD_OFFSET_X", "10"));
            HUD_OFFSET_Y = Integer.parseInt(values.getOrDefault("HUD_OFFSET_Y", "10"));
            HUD_POSITION_X = HudPositionsX.valueOf(values.getOrDefault("HUD_POSITION_X", "LEFT"));
            HUD_BG_OPACITY = Float.parseFloat(values.getOrDefault("HUD_BG_OPACITY", "0.5"));
        } catch (FileNotFoundException ignore){

        }
    }


}

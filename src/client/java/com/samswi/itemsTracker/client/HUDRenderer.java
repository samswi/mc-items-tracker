package com.samswi.itemsTracker.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix3x2fStack;

import java.util.HashMap;
import java.util.Map;

public class HUDRenderer implements ClientModInitializer {
    public static final Identifier EXAMPLE_LAYER = Identifier.of("allitemstracker", "hud-example-layer");
    public static double highlightEndTime = 0;
    public static Map<Integer, HUDItemDisplay> newestItemsMap = new HashMap<>();

    @Override
    public void onInitializeClient() {
        // Attach our rendering code to before the chat hud layer. Our layer will render right before the chat. The API will take care of z spacing and automatically add 200 after every layer.

    }

    public static int getFirstFreeIndex(){
        boolean found = false;
        int i = -1;
        while (!found){
            i++;
            if (!newestItemsMap.containsKey(i)) found = true;
        }
        return i;
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        TextWidget itemCountWidget = new TextWidget(Text.of(String.valueOf(ItemsTrackerClient.goalItems.size() - ItemsTrackerClient.remainingItems.size())), MinecraftClient.getInstance().textRenderer);
        TextWidget allItemsCountWidget = new TextWidget( Text.of(String.valueOf("/" + ItemsTrackerClient.goalItems.size())), MinecraftClient.getInstance().textRenderer);
        allItemsCountWidget.setPosition(itemCountWidget.getWidth()*3+3, 13);
        float percentDone = ItemsTrackerClient.goalItems.size()-ItemsTrackerClient.remainingItems.size();
        TextWidget percentageDoneWidget = new TextWidget(Text.of(String.format("%.1f%%", ((percentDone/ItemsTrackerClient.goalItems.size())*100))) , MinecraftClient.getInstance().textRenderer);
        percentageDoneWidget.setTextColor(0xFFAAAAAA);
        percentageDoneWidget.setPosition(itemCountWidget.getWidth()*3+3, 3);
        float offsetX = 10;
        float offsetY = 10;
        switch (ItemsTrackerConfig.HUD_POSITION_X){
            case LEFT:
                offsetX = ItemsTrackerConfig.HUD_OFFSET_X;
                break;
            case RIGHT:
                offsetX = context.getScaledWindowWidth() - ((itemCountWidget.getWidth()*3) + Math.max(allItemsCountWidget.getWidth(), percentageDoneWidget.getWidth()) + 3)*ItemsTrackerConfig.HUD_SCALE - ItemsTrackerConfig.HUD_OFFSET_X;
        }

        offsetY = ItemsTrackerConfig.HUD_OFFSET_Y;
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(offsetX, offsetY);
        matrices.scale(ItemsTrackerConfig.HUD_SCALE, ItemsTrackerConfig.HUD_SCALE);
        matrices.pushMatrix();
        int bg_alpha = (int)(255.0 * ItemsTrackerConfig.HUD_BG_OPACITY);
        int bg_color = (bg_alpha << 24);
        context.fill(-5, -5, ((itemCountWidget.getWidth()*3) + Math.max(allItemsCountWidget.getWidth(), percentageDoneWidget.getWidth()) + 3) + 5, itemCountWidget.getHeight()*3 + 2, bg_color);
        matrices.scale(3.0f, 3.0f);
        int color;
        if (Util.getMeasuringTimeMs()/1000.0 > highlightEndTime){
            color = 0xFFFFFFFF;
        } else if (highlightEndTime - Util.getMeasuringTimeMs()/1000.0 < 5) {
            float delta = (float) (1.0-((highlightEndTime - Util.getMeasuringTimeMs()/1000.0)/5));
            color = ColorHelper.lerp(delta, 0xFF00FF00, 0xFFFFFFFF);
        }
        else{
            color = 0xFF00FF00;
        }
        itemCountWidget.setTextColor(color);
        itemCountWidget.render(context, 0, 0, 0);
        matrices.popMatrix();
        allItemsCountWidget.setTextColor(0xFFAAAAAA);
        allItemsCountWidget.render(context, 0, 0, 0);
        percentageDoneWidget.render(context,0,0,0);
        matrices.popMatrix();
    }
}
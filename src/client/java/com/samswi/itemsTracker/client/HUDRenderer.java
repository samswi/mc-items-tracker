package com.samswi.itemsTracker.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix3x2fStack;

public class HUDRenderer {
    public static final Identifier IDENTIFIER = Identifier.of("itemstracker", "hud");
    public static double highlightEndTime = 0;

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
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
        TextWidget itemCountWidget = new TextWidget(MutableText.of(new PlainTextContent.Literal(String.valueOf(ItemsTrackerClient.goalItems.size() - ItemsTrackerClient.remainingItems.size()))).withColor(color), MinecraftClient.getInstance().textRenderer);
        TextWidget allItemsCountWidget = new TextWidget(MutableText.of(new PlainTextContent.Literal("/" + ItemsTrackerClient.goalItems.size())).withColor(0xFFAAAAAA), MinecraftClient.getInstance().textRenderer);
        allItemsCountWidget.setPosition(itemCountWidget.getWidth()*3+3, 13);
        float percentDone = ItemsTrackerClient.goalItems.size()-ItemsTrackerClient.remainingItems.size();
        TextWidget percentageDoneWidget = new TextWidget(MutableText.of(new PlainTextContent.Literal(String.format("%.1f%%", ((percentDone/ ItemsTrackerClient.goalItems.size())*100)))).withColor(0xFFAAAAAA) , MinecraftClient.getInstance().textRenderer);
        percentageDoneWidget.setPosition(itemCountWidget.getWidth()*3+3, 3);
        float offsetX;
        float offsetY;
        offsetX = switch (ItemsTrackerConfig.HUD_POSITION_X) {
            case LEFT -> ItemsTrackerConfig.HUD_OFFSET_X;
            case RIGHT ->
                    context.getScaledWindowWidth() - ((itemCountWidget.getWidth() * 3) + Math.max(allItemsCountWidget.getWidth(), percentageDoneWidget.getWidth()) + 3) * ItemsTrackerConfig.HUD_SCALE - ItemsTrackerConfig.HUD_OFFSET_X;
        };

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
        itemCountWidget.render(context, 0, 0, 0);
        matrices.popMatrix();
        allItemsCountWidget.render(context, 0, 0, 0);
        percentageDoneWidget.render(context,0,0,0);
        matrices.popMatrix();
    }
}
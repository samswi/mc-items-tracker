package com.samswi.itemsTracker.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;

public class HUDRenderer {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("itemstracker", "hud");
    public static double highlightEndTime = 0;

    public static void render(GuiGraphics context, DeltaTracker tickCounter) {
        if (ItemsTrackerClient.goalItems == null) return;
        int color;
        if (ItemsTrackerClient.remainingItems.isEmpty()){
            color = 0xFF00FF00;
        }
        else if (Util.getMillis()/1000.0 > highlightEndTime){
            color = 0xFFFFFFFF;
        } else if (highlightEndTime - Util.getMillis()/1000.0 < 5) {
            float delta = (float) (1.0-((highlightEndTime - Util.getMillis()/1000.0)/5));
            color = ARGB.srgbLerp(delta, 0xFF00FF00, 0xFFFFFFFF);
        }
        else{
            color = 0xFF00FF00;
        }
        StringWidget itemCountWidget = new StringWidget(MutableComponent.create(new PlainTextContents.LiteralContents(String.valueOf(ItemsTrackerClient.goalItems.size() - ItemsTrackerClient.remainingItems.size()))).withColor(color), Minecraft.getInstance().font);
        StringWidget allItemsCountWidget = new StringWidget(MutableComponent.create(new PlainTextContents.LiteralContents("/" + ItemsTrackerClient.goalItems.size())).withColor(0xFFAAAAAA), Minecraft.getInstance().font);
        allItemsCountWidget.setPosition(itemCountWidget.getWidth()*3+3, 13);
        float percentDone = ItemsTrackerClient.goalItems.size()-ItemsTrackerClient.remainingItems.size();
        StringWidget percentageDoneWidget = new StringWidget(MutableComponent.create(new PlainTextContents.LiteralContents(String.format("%.1f%%", ((percentDone/ ItemsTrackerClient.goalItems.size())*100)))).withColor(ItemsTrackerClient.remainingItems.isEmpty() ? 0xFF88FF88 : 0xFFAAAAAA) , Minecraft.getInstance().font);
        percentageDoneWidget.setPosition(itemCountWidget.getWidth()*3+3, 3);
        float offsetX;
        float offsetY;
        offsetX = switch (ItemsTrackerConfig.HUD_POSITION_X) {
            case LEFT -> ItemsTrackerConfig.HUD_OFFSET_X;
            case RIGHT ->
                    context.guiWidth() - ((itemCountWidget.getWidth() * 3) + Math.max(allItemsCountWidget.getWidth(), percentageDoneWidget.getWidth()) + 3) * ItemsTrackerConfig.HUD_SCALE - ItemsTrackerConfig.HUD_OFFSET_X;
        };

        offsetY = ItemsTrackerConfig.HUD_OFFSET_Y;
        Matrix3x2fStack matrices = context.pose();
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
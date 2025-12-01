package com.samswi.itemsTracker.client;

import com.sun.jna.platform.unix.X11;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
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
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(10, 10);
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
        TextWidget myTextWidget = new TextWidget(Text.of(String.valueOf(ItemsTrackerClient.goalItems.size() - ItemsTrackerClient.remainingItems.size())), MinecraftClient.getInstance().textRenderer);
        myTextWidget.setTextColor(color);
        myTextWidget.render(context, 0, 0, 0);
        matrices.popMatrix();
        context.drawText(MinecraftClient.getInstance().textRenderer, "/" + ItemsTrackerClient.goalItems.size(), myTextWidget.getWidth()*3+3+10, (myTextWidget.getY() + myTextWidget.getHeight())*3-3, 0xFFAAAAAA, true);
        float percentDone = ItemsTrackerClient.goalItems.size()-ItemsTrackerClient.remainingItems.size();
        context.drawText(MinecraftClient.getInstance().textRenderer, String.format("%.1f%%", ((percentDone/ItemsTrackerClient.goalItems.size())*100)), myTextWidget.getWidth()*3+3+10, (myTextWidget.getY() + myTextWidget.getHeight())*3-13, 0xFFAAAAAA, true);
        newestItemsMap.forEach((integer, hudItemDisplay) -> {
            hudItemDisplay.y = (myTextWidget.getHeight()*3+10)+(hudItemDisplay.index*18);
//            hudItemDisplay.render(context);
        });
    }
}
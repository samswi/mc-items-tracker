package com.samswi.itemsTracker.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;

public class HUDItemDisplay{
    public int x;
    public int y;
    public int index;
    double expiresAt;
    ItemStack myItemStack;

    public HUDItemDisplay(ItemStack itemStack, double expirationTime, int index) {
        expiresAt = expirationTime;
        myItemStack = itemStack;
        this.index = index;
        this.x = 10;
    }

    public void render(DrawContext context) {
        double timeLeft = expiresAt - Util.getMeasuringTimeMs() / 1000.0;
        float alpha = 1.0f;
        if (timeLeft <= 5.0) {
            double t = (5.0 - timeLeft) / 5.0;
            double eased = t * t;
            alpha = (float)Math.max(0.0, 1.0 - eased);
        }

        // Save current blending state if needed


//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
//        context.drawItem(myItemStack, x, y);
//        RenderSystem.disableBlend();
//        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);


        // Reset shader color and blending immediately after drawing


        // Now draw text with ARGB color fade
        int alphaInt = (int)(alpha * 255) << 24;
        int color = alphaInt | 0x00FF00; // Fading green text
        context.drawText(
                MinecraftClient.getInstance().textRenderer,
                myItemStack.getName(),
                x + 20, y + 4,
                color,
                true
        );
    }


}
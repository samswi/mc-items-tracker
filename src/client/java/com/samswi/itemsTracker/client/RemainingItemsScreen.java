package com.samswi.itemsTracker.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RemainingItemsScreen extends Screen {
    TextWidget myTextWidget;

    RemainingItemsScreen(){
        super(Text.of("Remaining Items"));

        myTextWidget = new TextWidget(Text.of("Remaining items"), MinecraftClient.getInstance().textRenderer);

    }



    @Override
    protected void init() {
        ItemsTrackerClient.remainingItems.forEach(s -> {
            ItemWidget myItemWidget = new ItemWidget(s);
            this.addDrawable(myItemWidget);
        });
        myTextWidget.setPosition((MinecraftClient.getInstance().getWindow().getScaledWidth()/2)- myTextWidget.getWidth()/2, 10);
        addDrawableChild(myTextWidget);
        super.init();
    }

    public class ItemWidget implements Drawable {
        Identifier itemId;
        int x;
        int y;
        public ItemStack myItemStack;

        public ItemWidget(String itemIdString) {
            this.itemId = Identifier.of(itemIdString);
            x = (int) (Math.random()* (MinecraftClient.getInstance().getWindow().getScaledWidth()-16));
            y = (int) (Math.random()* (MinecraftClient.getInstance().getWindow().getScaledHeight()-16));

            myItemStack = new ItemStack(Registries.ITEM.get(itemId));

        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            MinecraftClient client = MinecraftClient.getInstance();
            context.drawItem(myItemStack, x, y);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, myItemStack.getTooltip(Item.TooltipContext.DEFAULT, client.player, client.options.advancedItemTooltips ? TooltipType.Default.ADVANCED : TooltipType.Default.BASIC), mouseX, mouseY);
            }

        }
    }
}

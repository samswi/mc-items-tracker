package com.samswi.itemsTracker.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RemainingItemsScreen extends Screen {
    TextWidget myTextWidget;
    private ScrollableLayoutWidget scrollableLayoutWidget;
    GridWidget grid;
    GridWidget.Adder gridAdder;
    final MinecraftClient client = MinecraftClient.getInstance();
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);


    RemainingItemsScreen(){
        super(Text.of("Remaining Items"));

        myTextWidget = new TextWidget(Text.of("Remaining items"), MinecraftClient.getInstance().textRenderer);

        layout.addHeader(Text.of("Item Tracker"), client.textRenderer);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.close();
        }).width(200).build());
        grid = new GridWidget();
        grid.getMainPositioner()
                .margin(4);
        gridAdder = grid.createAdder(12);

        for (String i : ItemsTrackerClient.goalItems){
            BackgroundedItemStackWidget itemWidget;
            if (ItemsTrackerClient.remainingItems.contains(i)){
                itemWidget = new BackgroundedItemStackWidget(client, 0, 0, 16, 16, Text.of(""), new ItemStack(Registries.ITEM.get(Identifier.of(i))), true, true, 0x88000000);
            } else {
                itemWidget = new BackgroundedItemStackWidget(client, 0, 0, 16, 16, Text.of(""), new ItemStack(Registries.ITEM.get(Identifier.of(i))), true, true, 0xFF00FF00);
            }
            gridAdder.add(itemWidget);
        }

        scrollableLayoutWidget = new ScrollableLayoutWidget(client, grid, layout.getContentHeight());

        layout.addBody(scrollableLayoutWidget);

    }

    @Override
    protected void init() {
        super.init();

        layout.setPosition(0, 0);

        layout.refreshPositions();
        scrollableLayoutWidget.setHeight(layout.getContentHeight());
        scrollableLayoutWidget.refreshPositions();
        scrollableLayoutWidget.setPosition(scrollableLayoutWidget.getX(), layout.getHeaderHeight());
        grid.refreshPositions();
        layout.forEachChild(this::addDrawableChild);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR_TEXTURE, 0, this.height - this.layout.getFooterHeight(), 0.0F, 0.0F, this.width, 2, 32, 2);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR_TEXTURE, 0, this.layout.getHeaderHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        this.renderDarkening(context, 0, this.layout.getHeaderHeight(), this.width, layout.getContentHeight());
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    public static class BackgroundedItemStackWidget extends ItemStackWidget{
        int backgroundColor;

        public BackgroundedItemStackWidget(MinecraftClient client, int x, int y, int width, int height, Text message, ItemStack stack, boolean drawOverlay, boolean hasTooltip, int background) {
            super(client, x, y, width, height, message, stack, drawOverlay, hasTooltip);
            backgroundColor = background;

        }


        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            context.fill(this.getX()-4, this.getY()-4, this.getX()+this.getWidth()+4, this.getY()+this.getHeight()+4, backgroundColor);
            super.renderWidget(context, mouseX, mouseY, deltaTicks);
        }
    }
}


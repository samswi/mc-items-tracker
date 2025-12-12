package com.samswi.itemsTracker.client;

import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RemainingItemsScreen extends Screen {
    int rowWidth;
    private final ScrollableLayoutWidget scrollableLayoutWidget;
    final GridWidget grid;
    final GridWidget.Adder gridAdder;
    final MinecraftClient client = MinecraftClient.getInstance();
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    final ButtonWidget extendButton = ButtonWidget.builder(Text.of("⛶"), button -> client.setScreen(new RemainingItemsScreen(
            (client.getWindow().getScaledWidth() / 24) - 1
    ))).build();

    RemainingItemsScreen(int rowWidth){
        super(Text.of("Remaining Items"));
        this.rowWidth = rowWidth;

        layout.addHeader(Text.of("Item Tracker"), client.textRenderer);
        extendButton.setPosition(client.getWindow().getScaledWidth() - 26, 6);
        extendButton.setDimensions(20, 20);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.close()).width(200).build());
        grid = new GridWidget();
        grid.getMainPositioner()
                .margin(4);
        gridAdder = grid.createAdder(rowWidth);

        for (String i : ItemsTrackerClient.goalItems){
            ItemStack itemStack = ItemsTracker.parseItem(i);
            BackgroundedItemStackWidget itemWidget;
            if (ItemsTrackerClient.remainingItems.contains(i)){
                itemWidget = new BackgroundedItemStackWidget(client, 0, 0, 16, 16, Text.of(""), itemStack, true, true, 0x88000000);
            } else {
                itemWidget = new BackgroundedItemStackWidget(client, 0, 0, 16, 16, Text.of(""), itemStack, true, true, 0xFF00FF00);
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
        this.addDrawableChild(extendButton);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR_TEXTURE, 0, this.height - this.layout.getFooterHeight(), 0.0F, 0.0F, this.width, 2, 32, 2);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR_TEXTURE, 0, this.layout.getHeaderHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        this.renderDarkening(context, 0, this.layout.getHeaderHeight(), this.width, layout.getContentHeight());
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    public static class BackgroundedItemStackWidget extends ItemStackWidget{
        final int backgroundColor;

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


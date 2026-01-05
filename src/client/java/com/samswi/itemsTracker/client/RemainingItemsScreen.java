package com.samswi.itemsTracker.client;

import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicBoolean;

public class RemainingItemsScreen extends Screen {
    boolean fullscreen = false;
    private ScrollableLayoutWidget scrollableLayoutWidget;
    GridWidget grid;
    GridWidget.Adder gridAdder;
    final MinecraftClient client = MinecraftClient.getInstance();
    public  ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    final TextFieldWidget searchBar = new TextFieldWidget(client.textRenderer, 200, 15, Text.of("Search"));

    final ButtonWidget extendButton = ButtonWidget.builder(Text.of("⛶"), button -> {
                fullscreen = !fullscreen;
                this.init();
            }).build();

    RemainingItemsScreen(int rowWidth){
        super(Text.of("Remaining Items"));

        searchBar.setChangedListener(s -> {
           this.init();
        });

    }

    @Override
    protected void init() {
        this.clearChildren();
        this.blur();
        super.init();
        setFocused(searchBar);
        int rowWidth = fullscreen ? (client.getWindow().getScaledWidth() / 24) - 1 : 12;

        layout = new ThreePartsLayoutWidget(this);
        layout.addHeader(searchBar);
        extendButton.setPosition(client.getWindow().getScaledWidth() - 26, 6);
        extendButton.setDimensions(20, 20);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.close()).width(200).build());
        grid = new GridWidget();
        grid.getMainPositioner()
                .margin(4);
        gridAdder = grid.createAdder(rowWidth);

        String filter = searchBar.getText().toLowerCase();

        for (String i : ItemsTrackerClient.goalItems){
            ItemStack itemStack = ItemsTracker.parseItem(i);
            BackgroundedItemStackWidget itemWidget;
            if (ItemsTrackerClient.remainingItems.contains(i)){
                itemWidget = new BackgroundedItemStackWidget(client, 0, 0, 16, 16, Text.of(""), itemStack, true, true, 0x88000000);
            } else {
                itemWidget = new BackgroundedItemStackWidget(client, 0, 0, 16, 16, Text.of(""), itemStack, true, true, 0xFF00FF00);
            }
            AtomicBoolean shouldDisplay = new AtomicBoolean(filter.isEmpty());

            if (itemStack.getName().getString().toLowerCase().contains(filter)) shouldDisplay.set(true);
            else {
                itemStack.getTooltip(Item.TooltipContext.DEFAULT, client.player, client.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC).forEach(text -> {
                    if (text.toString().toLowerCase().contains(filter)) shouldDisplay.set(true);
                });
            }

            if (shouldDisplay.get()) gridAdder.add(itemWidget);
        }

        scrollableLayoutWidget = new ScrollableLayoutWidget(client, grid, layout.getContentHeight());
        layout.addBody(scrollableLayoutWidget);

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


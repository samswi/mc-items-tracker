package com.samswi.itemsTracker.client;

import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ItemDisplayWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemainingItemsScreen extends Screen {
    boolean fullscreen = false;
    private ScrollableLayout scrollableLayoutWidget;
    GridLayout grid;
    GridLayout.RowHelper gridAdder;
    final Minecraft client = Minecraft.getInstance();
    public  HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    final EditBox searchBar = new EditBox(client.font, 200, 15, Component.nullToEmpty("Search"));

    final Button extendButton = Button.builder(Component.nullToEmpty("⛶"), button -> {
                fullscreen = !fullscreen;
                this.init();
            }).build();

    RemainingItemsScreen(int rowWidth){
        super(Component.nullToEmpty("Remaining Items"));

        searchBar.setResponder(s -> {
           this.init();
        });

    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.clearFocus();
        super.init();
        setFocused(searchBar);
        int rowWidth = fullscreen ? (minecraft.getWindow().getGuiScaledWidth() / 24) - 1 : 12;

        layout = new HeaderAndFooterLayout(this);
        layout.addToHeader(searchBar);
        extendButton.setPosition(minecraft.getWindow().getGuiScaledWidth() - 26, 6);
        extendButton.setSize(20, 20);
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose()).width(200).build());
        grid = new GridLayout();
        grid.defaultCellSetting()
                .padding(4);
        gridAdder = grid.createRowHelper(rowWidth);

        String filter = searchBar.getValue().toLowerCase();

        if (ItemsTrackerClient.goalItems != null) {
            for (String i : ItemsTrackerClient.goalItems) {
                ItemStack itemStack = ItemsTracker.parseItem(i);
                BackgroundedItemStackWidget itemWidget;
                if (ItemsTrackerClient.remainingItems.contains(i)) {
                    itemWidget = new BackgroundedItemStackWidget(minecraft, 0, 0, 16, 16, Component.nullToEmpty(""), itemStack, true, true, 0x88000000);
                } else {
                    itemWidget = new BackgroundedItemStackWidget(minecraft, 0, 0, 16, 16, Component.nullToEmpty(""), itemStack, true, true, 0xFF00FF00);
                }
                AtomicBoolean shouldDisplay = new AtomicBoolean(filter.isEmpty());

                if (itemStack.getHoverName().getString().toLowerCase().contains(filter)) shouldDisplay.set(true);
                else {
                    itemStack.getTooltipLines(Item.TooltipContext.EMPTY, minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL).forEach(text -> {
                        if (text.toString().toLowerCase().contains(filter)) shouldDisplay.set(true);
                    });
                }

                if (shouldDisplay.get()) gridAdder.addChild(itemWidget);
            }
        } else {
            gridAdder.addChild(new StringWidget(Component.nullToEmpty("This server doesn't support the items tracker mod"), minecraft.font));
        }

        scrollableLayoutWidget = new ScrollableLayout(minecraft, grid, layout.getContentHeight());
        layout.addToContents(scrollableLayoutWidget);

        layout.setPosition(0, 0);

        layout.arrangeElements();
        scrollableLayoutWidget.setMaxHeight(layout.getContentHeight());
        scrollableLayoutWidget.arrangeElements();
        scrollableLayoutWidget.setPosition(scrollableLayoutWidget.getX(), layout.getHeaderHeight());
        grid.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
        this.addRenderableWidget(extendButton);

    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        context.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight(), 0.0F, 0.0F, this.width, 2, 32, 2);
        context.blit(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR, 0, this.layout.getHeaderHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        this.renderMenuBackground(context, 0, this.layout.getHeaderHeight(), this.width, layout.getContentHeight());
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    public static class BackgroundedItemStackWidget extends ItemDisplayWidget{
        final int backgroundColor;

        public BackgroundedItemStackWidget(Minecraft client, int x, int y, int width, int height, Component message, ItemStack stack, boolean drawOverlay, boolean hasTooltip, int background) {
            super(client, x, y, width, height, message, stack, drawOverlay, hasTooltip);
            backgroundColor = background;

        }


        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
            context.fill(this.getX()-4, this.getY()-4, this.getX()+this.getWidth()+4, this.getY()+this.getHeight()+4, backgroundColor);
            super.renderWidget(context, mouseX, mouseY, deltaTicks);
        }
    }
}


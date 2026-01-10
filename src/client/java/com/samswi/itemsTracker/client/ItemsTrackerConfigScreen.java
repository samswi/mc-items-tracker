package com.samswi.itemsTracker.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ItemsTrackerConfigScreen extends Screen {

    private final ScrollableLayout scrollableLayoutWidget;
    final GridLayout grid;
    final GridLayout.RowHelper gridAdder;
    final Minecraft client = Minecraft.getInstance();
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    final AbstractSliderButton HUD_SCALE = new AbstractSliderButton(0, 0, 408, 20, Component.nullToEmpty("HUD Scale: " + ItemsTrackerConfig.HUD_SCALE), 100) {
        @Override
        protected void updateMessage() {
            this.setMessage(Component.nullToEmpty("HUD Scale: " + Math.round(((this.value*5)*100))/100.0));
        }

        @Override
        protected void applyValue() {
            ItemsTrackerConfig.HUD_SCALE = (float) (this.value*5);
        }
    };

    final AbstractSliderButton HUD_OFFSET_X = new AbstractSliderButton(0, 0, 408, 20, Component.nullToEmpty("HUD Offset X: " + ItemsTrackerConfig.HUD_OFFSET_X), 1000) {
        @Override
        protected void updateMessage() {
            this.setMessage(Component.nullToEmpty("HUD Offset X: " + (int)(this.value*1000)));
        }

        @Override
        protected void applyValue() {
            ItemsTrackerConfig.HUD_OFFSET_X = (int)(this.value*1000.0);
        }
    };

    final AbstractSliderButton HUD_OFFSET_Y = new AbstractSliderButton(0, 0, 408, 20, Component.nullToEmpty("HUD Offset Y: " + ItemsTrackerConfig.HUD_OFFSET_Y), 1000) {
        @Override
        protected void updateMessage() {
            this.setMessage(Component.nullToEmpty("HUD Offset Y: " + (int)(this.value*1000.0)));
        }

        @Override
        protected void applyValue() {
            ItemsTrackerConfig.HUD_OFFSET_Y = (int)(this.value*1000.0);
        }
    };

    final AbstractSliderButton HUD_BG_OPACITY = new AbstractSliderButton(0, 0, 200, 20, Component.nullToEmpty("HUD Background opacity: " + ItemsTrackerConfig.HUD_BG_OPACITY), 100) {
        @Override
        protected void updateMessage() {
            this.setMessage(Component.nullToEmpty("HUD Background opacity: " + Math.round(((this.value*100)*100)/100) + "%"));
        }

        @Override
        protected void applyValue() {
            ItemsTrackerConfig.HUD_BG_OPACITY = (float) this.value;
        }
    };

    final Button ANCHOR = Button.builder(Component.nullToEmpty("HUD Anchor: " + ItemsTrackerConfig.HUD_POSITION_X), button -> {
        if (ItemsTrackerConfig.HUD_POSITION_X == ItemsTrackerConfig.HudPositionsX.LEFT){
            ItemsTrackerConfig.HUD_POSITION_X = ItemsTrackerConfig.HudPositionsX.RIGHT;
        } else if (ItemsTrackerConfig.HUD_POSITION_X == ItemsTrackerConfig.HudPositionsX.RIGHT) {
            ItemsTrackerConfig.HUD_POSITION_X = ItemsTrackerConfig.HudPositionsX.LEFT;
        }
        button.setMessage(Component.nullToEmpty("HUD Anchor: " + ItemsTrackerConfig.HUD_POSITION_X));
    }).build();

    public ItemsTrackerConfigScreen() {
        super(Component.nullToEmpty("Items tracker config"));
        ANCHOR.setSize(200, 20);
        layout.addTitleHeader(Component.nullToEmpty("Items tracker config"), client.font);
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose()).width(200).build());

        grid = new GridLayout();
        grid.defaultCellSetting()
                .padding(4);
        gridAdder = grid.createRowHelper(2);

        gridAdder.addChild(HUD_SCALE, 2);
        gridAdder.addChild(HUD_OFFSET_X, 2);
        gridAdder.addChild(HUD_OFFSET_Y, 2);
        gridAdder.addChild(HUD_BG_OPACITY);
        gridAdder.addChild(ANCHOR);

        scrollableLayoutWidget = new ScrollableLayout(minecraft, grid, layout.getContentHeight());

        layout.addToContents(scrollableLayoutWidget);

    }

    @Override
    protected void init() {
        super.init();

        layout.setPosition(0, 0);

        layout.arrangeElements();
        scrollableLayoutWidget.setMaxHeight(layout.getContentHeight());
        scrollableLayoutWidget.arrangeElements();
        scrollableLayoutWidget.setPosition(scrollableLayoutWidget.getX(), layout.getHeaderHeight());
        grid.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);

    }

    @Override
    public void onClose() {
        ItemsTrackerConfig.saveConfig();
        super.onClose();
    }
}

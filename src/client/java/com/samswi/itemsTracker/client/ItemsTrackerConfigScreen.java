package com.samswi.itemsTracker.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ItemsTrackerConfigScreen extends Screen {

    private final ScrollableLayoutWidget scrollableLayoutWidget;
    final GridWidget grid;
    final GridWidget.Adder gridAdder;
    final MinecraftClient client = MinecraftClient.getInstance();
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    final SliderWidget HUD_SCALE = new SliderWidget(0, 0, 408, 20, Text.of("HUD Scale: " + ItemsTrackerConfig.HUD_SCALE), 100) {
        @Override
        protected void updateMessage() {
            this.setMessage(Text.of("HUD Scale: " + Math.round(((this.value*5)*100))/100.0));
        }

        @Override
        protected void applyValue() {
            ItemsTrackerConfig.HUD_SCALE = (float) (this.value*5);
        }
    };

    final SliderWidget HUD_OFFSET_X = new SliderWidget(0, 0, 408, 20, Text.of("HUD Offset X: " + ItemsTrackerConfig.HUD_OFFSET_X), 1000) {
        @Override
        protected void updateMessage() {
            this.setMessage(Text.of("HUD Offset X: " + (int)(this.value*1000)));
        }

        @Override
        protected void applyValue() {
            ItemsTrackerConfig.HUD_OFFSET_X = (int)(this.value*1000.0);
        }
    };

    final SliderWidget HUD_OFFSET_Y = new SliderWidget(0, 0, 408, 20, Text.of("HUD Offset Y: " + ItemsTrackerConfig.HUD_OFFSET_Y), 1000) {
        @Override
        protected void updateMessage() {
            this.setMessage(Text.of("HUD Offset Y: " + (int)(this.value*1000.0)));
        }

        @Override
        protected void applyValue() {
            ItemsTrackerConfig.HUD_OFFSET_Y = (int)(this.value*1000.0);
        }
    };

    final SliderWidget HUD_BG_OPACITY = new SliderWidget(0, 0, 200, 20, Text.of("HUD Background opacity: " + ItemsTrackerConfig.HUD_BG_OPACITY), 100) {
        @Override
        protected void updateMessage() {
            this.setMessage(Text.of("HUD Background opacity: " + Math.round(((this.value*100)*100)/100) + "%"));
        }

        @Override
        protected void applyValue() {
            ItemsTrackerConfig.HUD_BG_OPACITY = (float) this.value;
        }
    };

    final ButtonWidget ANCHOR = ButtonWidget.builder(Text.of("HUD Anchor: " + ItemsTrackerConfig.HUD_POSITION_X), button -> {
        if (ItemsTrackerConfig.HUD_POSITION_X == ItemsTrackerConfig.HudPositionsX.LEFT){
            ItemsTrackerConfig.HUD_POSITION_X = ItemsTrackerConfig.HudPositionsX.RIGHT;
        } else if (ItemsTrackerConfig.HUD_POSITION_X == ItemsTrackerConfig.HudPositionsX.RIGHT) {
            ItemsTrackerConfig.HUD_POSITION_X = ItemsTrackerConfig.HudPositionsX.LEFT;
        }
        button.setMessage(Text.of("HUD Anchor: " + ItemsTrackerConfig.HUD_POSITION_X));
    }).build();

    public ItemsTrackerConfigScreen() {
        super(Text.of("Items tracker config"));
        ANCHOR.setDimensions(200, 20);
        layout.addHeader(Text.of("Items tracker config"), client.textRenderer);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.close()).width(200).build());

        grid = new GridWidget();
        grid.getMainPositioner()
                .margin(4);
        gridAdder = grid.createAdder(2);

        gridAdder.add(HUD_SCALE, 2);
        gridAdder.add(HUD_OFFSET_X, 2);
        gridAdder.add(HUD_OFFSET_Y, 2);
        gridAdder.add(HUD_BG_OPACITY);
        gridAdder.add(ANCHOR);

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
    public void close() {
        ItemsTrackerConfig.saveConfig();
        super.close();
    }
}

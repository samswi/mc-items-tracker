package com.samswi.itemsTracker.mixin;

import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class CraftingScreenHandlerMixin {

    @Inject(at = @At("HEAD"), method = "slotsChanged")
    private void checkForItem(Container inventory, CallbackInfo ci){

        for (Slot slot : ((CraftingMenu)(Object)this).getInputGridSlots()){
            if (ItemsTracker.currentServer == null) return;
            ItemsTracker.removeItemFromRemainingItems(slot.getItem(), ((CraftingMenu)(Object)this).player);
        }

    }
}

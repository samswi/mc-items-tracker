package com.samswi.itemsTracker.mixin;

import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Inject(at = @At("HEAD"), method = "onContentChanged")
    private void checkForItem(Inventory inventory, CallbackInfo ci){

        for (Slot slot : ((CraftingScreenHandler)(Object)this).getInputSlots()){
            if (ItemsTracker.currentServer == null) return;
            ItemsTracker.removeItemFromRemainingItems(slot.getStack().getRegistryEntry().getIdAsString(), ((CraftingScreenHandler)(Object)this).player);
        }

    }
}

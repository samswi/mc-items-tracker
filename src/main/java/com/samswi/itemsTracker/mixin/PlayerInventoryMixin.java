package com.samswi.itemsTracker.mixin;

import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class PlayerInventoryMixin {

    @Inject(at = @At("HEAD"), method = "addResource(ILnet/minecraft/world/item/ItemStack;)I")
    void checkForItem(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir){
        if (ItemsTracker.currentServer == null) return;
        ItemsTracker.removeItemFromRemainingItems(stack, ((Inventory)(Object)this).player);
    }

    @Inject(at = @At("HEAD"), method = "setItem")
    void checkForItem(int slot, ItemStack stack, CallbackInfo ci){
        if (ItemsTracker.currentServer == null) return;
        ItemsTracker.removeItemFromRemainingItems(stack, ((Inventory)(Object)this).player);
    }


}

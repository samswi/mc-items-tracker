package com.samswi.itemsTracker.mixin;

import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Shadow
    @Final
    public PlayerEntity player;

    @Inject(at = @At("HEAD"), method = "addStack(ILnet/minecraft/item/ItemStack;)I")
    void checkForItem(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir){
        if (ItemsTracker.currentServer == null) return;
        ItemsTracker.removeItemFromRemainingItems(stack.getRegistryEntry().getIdAsString(), ((PlayerInventory)(Object)this).player);
    }

    @Inject(at = @At("HEAD"), method = "setStack")
    void checkForItem(int slot, ItemStack stack, CallbackInfo ci){
        if (ItemsTracker.currentServer == null) return;
        ItemsTracker.removeItemFromRemainingItems(stack.getRegistryEntry().getIdAsString(), ((PlayerInventory)(Object)this).player);
    }


}

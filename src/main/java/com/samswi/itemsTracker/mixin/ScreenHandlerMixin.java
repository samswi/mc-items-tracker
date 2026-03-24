package com.samswi.itemsTracker.mixin;

import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerMixin {

    @Inject(at = @At("TAIL"), method = "clicked")
    public void checkForItem(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci){
        if (ItemsTracker.currentServer == null) return;
        ItemsTracker.removeItemFromRemainingItems(((AbstractContainerMenu)(Object)this).getCarried(), player);
    }
}

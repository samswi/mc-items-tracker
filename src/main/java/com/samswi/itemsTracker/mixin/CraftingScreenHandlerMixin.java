package com.samswi.itemsTracker.mixin;

import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.AbstractCraftingScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Shadow
    @Final
    private PlayerEntity player;

    @Inject(at = @At("HEAD"), method = "onContentChanged")
    private void checkForItem(Inventory inventory, CallbackInfo ci){

        for (Slot slot : ((CraftingScreenHandler)(Object)this).getInputSlots()){
            if (ItemsTracker.currentServer == null) return;
            ItemsTracker.removeItemFromRemainingItems(slot.getStack().getRegistryEntry().getIdAsString(), ((CraftingScreenHandler)(Object)this).player);
        }

    }
}

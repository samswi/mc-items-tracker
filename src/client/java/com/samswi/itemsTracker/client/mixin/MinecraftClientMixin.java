package com.samswi.itemsTracker.client.mixin;

import com.samswi.itemsTracker.client.ItemsTrackerClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/MinecraftClient;onDisconnected()V")
    public void clean(CallbackInfo ci){
        ItemsTrackerClient.goalItems = null;
        ItemsTrackerClient.remainingItems = null;
    }
}

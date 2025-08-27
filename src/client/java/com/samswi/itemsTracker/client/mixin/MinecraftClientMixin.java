package com.samswi.itemsTracker.client.mixin;

import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public abstract ProfileKeys getProfileKeys();

    @Inject(at = @At("TAIL"), method = "joinWorld")
    public void debug(ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci){
        if (ItemsTracker.currentServer == null) return;
        System.out.println("Joined the world");
    }
}

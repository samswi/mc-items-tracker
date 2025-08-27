package com.samswi.itemsTracker.mixin;

import com.mojang.datafixers.DataFixer;
import com.samswi.itemsTracker.ItemsTracker;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin{

    @Inject(at = @At("TAIL"), method = "<init>")
    public void catchServer(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        System.out.println("Server initialized");
        ItemsTracker.onServerCreation(((MinecraftServer) (Object) this));

    }

    @Inject(at = @At("HEAD"), method = "shutdown")
    public void onShutdown(CallbackInfo ci){
        ItemsTracker.onServerExit();
    }
}

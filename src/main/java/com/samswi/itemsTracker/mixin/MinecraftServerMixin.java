package com.samswi.itemsTracker.mixin;

import com.mojang.datafixers.DataFixer;
import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.util.ApiServices;
import net.minecraft.world.chunk.ChunkLoadProgress;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin{

//    @Inject(at = @At("TAIL"), method = "<init>")
//    public void catchServer(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, ChunkLoadProgress chunkLoadProgress, CallbackInfo ci) {
//        ItemsTracker.onServerCreation(((MinecraftServer) (Object) this));
//
//    }

    @Inject(at = @At("TAIL"), method = "createWorlds")
    public void initItemTracker(CallbackInfo ci){
        ItemsTracker.onServerCreation(((MinecraftServer) (Object) this));
    }

    @Inject(at = @At("HEAD"), method = "shutdown")
    public void onShutdown(CallbackInfo ci){
        ItemsTracker.onServerExit();
    }

    @Inject(at=@At("HEAD"), method = "save")
    public void injectItemTrackerSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir){
        ItemsTracker.saveItemsToFile();
    }

    @Inject(at=@At("HEAD"), method = "tick")
    public void injectSendActionBarMessage(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
        ItemsTracker.sendActionBarText();
    }
}

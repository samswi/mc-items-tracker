package com.samswi.itemsTracker.mixin;

import com.mojang.datafixers.DataFixer;
import com.samswi.itemsTracker.ItemsTracker;
import net.minecraft.server.MinecraftServer;
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

    @Inject(at = @At("TAIL"), method = "createLevels")
    public void initItemTracker(CallbackInfo ci){
        ItemsTracker.onServerCreation(((MinecraftServer) (Object) this));
    }

    @Inject(at = @At("HEAD"), method = "stopServer")
    public void onShutdown(CallbackInfo ci){
        ItemsTracker.onServerExit();
    }

    @Inject(at=@At("HEAD"), method = "saveAllChunks")
    public void injectItemTrackerSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir){
        ItemsTracker.saveItemsToFile();
    }

    @Inject(at=@At("HEAD"), method = "tickServer")
    public void injectSendActionBarMessage(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
        ItemsTracker.sendActionBarText();
    }
}

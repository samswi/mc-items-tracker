package com.samswi.itemsTracker.mixin;

import com.samswi.itemsTracker.ItemsTracker;
import com.samswi.itemsTracker.NetworkingStuff;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    void sendItemsList(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci){
        if (ItemsTracker.currentServer == null) return;
        ServerPlayNetworking.send(player, new NetworkingStuff.OnJoinPayload(ItemsTracker.remainingItemsList, ItemsTracker.goalItemsList));
        System.out.println("Sent onJoin packet");
    }
}

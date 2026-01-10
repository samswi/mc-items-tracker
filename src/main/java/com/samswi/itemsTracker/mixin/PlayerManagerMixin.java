package com.samswi.itemsTracker.mixin;

import com.samswi.itemsTracker.ItemsTracker;
import com.samswi.itemsTracker.NetworkingStuff;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {

    @Inject(at = @At("TAIL"), method = "placeNewPlayer")
    void sendItemsList(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci){
        if (ItemsTracker.currentServer == null) return;
        ServerPlayNetworking.send(player, new NetworkingStuff.OnJoinPayload(ItemsTracker.remainingItemsList, ItemsTracker.goalItemsList));
    }
}

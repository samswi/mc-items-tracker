package com.samswi.itemsTracker.client;

import com.samswi.itemsTracker.NetworkingStuff;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.List;

public class ItemsTrackerClient implements ClientModInitializer {

    public static List<String> remainingItems;
    public static List<String> goalItems;
    private static KeyMapping keyBinding;

    public static final File configFile = new File(FabricLoader.getInstance().getConfigDir() + "/itemstracker/config.txt");

    @Override
    public void onInitializeClient() {
        ItemsTrackerConfig.loadConfig();

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, HUDRenderer.IDENTIFIER, HUDRenderer::render);

        ClientCommandRegistrationCallback.EVENT.register(ItemsTrackerCommand::register);

        ClientPlayNetworking.registerGlobalReceiver(NetworkingStuff.OnJoinPayload.ID, ((payload, context) -> {
            remainingItems = payload.remainingItems();
            goalItems = payload.goalItems();
            ClientPlayNetworking.send(new NetworkingStuff.HandshakePayload());
        }));

        ClientPlayNetworking.registerGlobalReceiver(NetworkingStuff.RemoveItemPayload.ID, (payload, context) -> {
            remainingItems.remove(payload.itemId());
            HUDRenderer.highlightEndTime = (Util.getMillis() / 1000.0) + 5;
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingStuff.ShowToastPayload.ID, (payload, context) -> {
            Minecraft.getInstance().getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.WORLD_ACCESS_FAILURE, Component.literal(payload.title()), Component.literal(payload.description())));
        });

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "Open remaining items screen",
                GLFW.GLFW_KEY_X,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (keyBinding.isDown()){
                minecraftClient.setScreen(new RemainingItemsScreen(12));
            }
        });

    }

}

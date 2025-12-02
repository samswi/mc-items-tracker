package com.samswi.itemsTracker.client;

import com.google.gson.*;
import com.samswi.itemsTracker.ItemsTracker;
import com.samswi.itemsTracker.NetworkingStuff;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.impl.resource.v1.ResourceLoaderImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.samswi.itemsTracker.client.HUDRenderer.EXAMPLE_LAYER;
import static com.samswi.itemsTracker.client.HUDRenderer.getFirstFreeIndex;

public class ItemsTrackerClient implements ClientModInitializer {

    public static List<String> remainingItems;
    public static List<String> goalItems;
    private static KeyBinding keyBinding;

    public static File configFile = new File(FabricLoader.getInstance().getConfigDir() + "/itemstracker/config.txt");


    Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onInitializeClient() {
        ItemsTrackerConfig.loadConfig();

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of("items_tracker_hud"), HUDRenderer::render);

        ClientCommandRegistrationCallback.EVENT.register(ItemsTrackerCommand::register);

        ClientPlayNetworking.registerGlobalReceiver(NetworkingStuff.OnJoinPayload.ID, ((payload, context) -> {
            remainingItems = payload.remainingItems();
            goalItems = payload.goalItems();
        }));

        ClientPlayNetworking.registerGlobalReceiver(NetworkingStuff.RemoveItemPayload.ID, (payload, context) -> {
            remainingItems.remove(payload.itemId());
            HUDRenderer.highlightEndTime = (Util.getMeasuringTimeMs() / 1000.0) + 5;
            HUDRenderer.newestItemsMap.put(getFirstFreeIndex(), new HUDItemDisplay(new ItemStack(Registries.ITEM.get(Identifier.of(payload.itemId()))), ((Util.getMeasuringTimeMs()/1000.0)+10), getFirstFreeIndex()));
        });

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open remaining items screen",
                GLFW.GLFW_KEY_X,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (keyBinding.wasPressed()){
                minecraftClient.setScreen(new RemainingItemsScreen());
            }
        });

    }

    public static boolean isServerIntegrated(MinecraftServer server){
        return (server instanceof IntegratedServer);
    }


}

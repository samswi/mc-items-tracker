package com.samswi.itemsTracker.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ItemsTrackerCommand {
    static MinecraftClient client = MinecraftClient.getInstance();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandregistryaccess){


        dispatcher.register(literal("itemstracker")
                .then(literal("view_remaining")
                        .executes(commandContext -> {
                            Executors.newScheduledThreadPool(1).schedule(()  -> {
                                client.execute(() -> {
                                    client.setScreen(new RemainingItemsScreen(12));
                                });
                            },  1, TimeUnit.MILLISECONDS);
                            return 0;
                        }))
                .then(literal("config")
                        .executes(commandContext -> {
                            Executors.newScheduledThreadPool(1).schedule(()  -> {
                                client.execute(() -> {
                                    client.setScreen(new ItemsTrackerConfigScreen());
                                });
                            },  1, TimeUnit.MILLISECONDS);
                            return 0;
                        })));
    }
}

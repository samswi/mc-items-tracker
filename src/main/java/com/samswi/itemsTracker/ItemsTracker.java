package com.samswi.itemsTracker;

import com.google.gson.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ItemsTracker implements ModInitializer {

    public static MinecraftServer currentServer;
    public static Path worldFolder;
    public static File collectedItemsFile;
    public static Gson myGson = new Gson();

    public static File configFolder;
    public static File blacklistFile;

    public static List<String> lastItems = new ArrayList<>(50);
    public static int lastItemsIterator = 0;

    public static List<String> fullItemsList;
    public static List<String> goalItemsList;
    public static List<String> collectedItemsList;
    public static List<String> remainingItemsList;
    public static JsonArray collectedItemsJsonArray;


    @Override
    public void onInitialize() {
        configFolder = new File(FabricLoader.getInstance().getConfigDir() + "/allitems");
        configFolder.mkdirs();
        blacklistFile = new File(configFolder +  "/blacklist.txt");
        System.out.println(Registries.ITEM.size());
        fullItemsList = new ArrayList<>(Registries.ITEM.size());
        Registries.ITEM.forEach((item) -> {
            fullItemsList.add(item.toString());
        });
        ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> {
            minecraftServer.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                 if (goalItemsList != null && remainingItemsList != null)serverPlayerEntity.sendMessage(Text.of(String.valueOf(goalItemsList.size() - remainingItemsList.size())), true);
            });
        });
        PayloadTypeRegistry.playS2C().register(NetworkingStuff.OnJoinPayload.ID, NetworkingStuff.OnJoinPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingStuff.RemoveItemPayload.ID, NetworkingStuff.RemoveItemPayload.CODEC);

        if (!blacklistFile.exists()){
            try (InputStream in = ItemsTracker.class.getResourceAsStream("/default_blacklist.txt")) {
                Files.copy(in, blacklistFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void onServerCreation(MinecraftServer server) {
        currentServer = server;
        worldFolder = currentServer.getSavePath(WorldSavePath.ROOT).toAbsolutePath();
        collectedItemsFile = new File(worldFolder + "collected_items.txt");
        if (!blacklistFile.exists()){
            try (InputStream in = ItemsTracker.class.getResourceAsStream("/default_blacklist.txt")) {
                Files.copy(in, blacklistFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        JsonArray blacklistJson = new JsonArray();
        try {
            blacklistJson = loadJsonArrayFromFile(blacklistFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        goalItemsList = new ArrayList<>(fullItemsList);
        blacklistJson.forEach(jsonElement -> {
            String blacklistEntry = jsonElement.getAsString();
            if (blacklistEntry.contains("minecraft:")) {
                goalItemsList.removeIf(s -> s.equals(blacklistEntry));
            } else {
                goalItemsList.removeIf(s -> s.contains(blacklistEntry));
            }
        });
        collectedItemsList = new ArrayList<>(goalItemsList.size());
        if (!collectedItemsFile.exists()) {
            try {
                collectedItemsJsonArray = new JsonArray(goalItemsList.size());
                collectedItemsFile.createNewFile();

                saveJsonArrayToFile(collectedItemsJsonArray, collectedItemsFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                collectedItemsJsonArray = loadJsonArrayFromFile(collectedItemsFile);
                collectedItemsJsonArray.forEach((jsonElement -> {
                    collectedItemsList.add(jsonElement.getAsString());
                }));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        remainingItemsList = new ArrayList<>(goalItemsList);
        if (collectedItemsList != null) {
            collectedItemsList.forEach(s -> {
                remainingItemsList.remove(s);
            });
        }

    }

    public static void removeItemFromRemainingItems(String itemId, PlayerEntity player) {
        if (currentServer.getOverworld().isClient() && currentServer.isDedicated()) return;
        if (!lastItems.contains(itemId)) {
            if (remainingItemsList.remove(itemId)) {
                collectedItemsList.add(itemId);
                Text text = player.getStyledDisplayName().copy().append(Text.of(" obtained ").copyContentOnly().fillStyle(Style.EMPTY).withColor(0xFFAAAAAA)).append(new ItemStack(Registries.ITEM.get(Identifier.of(itemId))).toHoverableText());
                System.out.println("removed " + itemId + " from list");
                currentServer.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                    ServerPlayNetworking.send(serverPlayerEntity, new NetworkingStuff.RemoveItemPayload(itemId, remainingItemsList.size()));
                    serverPlayerEntity.sendMessage(text);
                });

            }
            lastItems.add(lastItemsIterator, itemId);
            lastItemsIterator++;
            if (lastItemsIterator >= 50) lastItemsIterator = 0;
        }
    }

    public static void onServerExit() {
        if (currentServer.getOverworld().isClient() && currentServer.isDedicated()) return;
        saveItemsToFile();
        remainingItemsList = null;
        lastItems = new ArrayList<>(50);
        lastItemsIterator = 0;
        currentServer = null;
        worldFolder = null;
    }

    public static void saveItemsToFile() {
        if (currentServer.getOverworld().isClient() && currentServer.isDedicated()) return;
        collectedItemsJsonArray = new JsonArray(collectedItemsList.size());
        collectedItemsList.forEach(collectedItemsJsonArray::add);
        System.out.println(currentServer.getSavePath(WorldSavePath.ROOT).toAbsolutePath());
        saveJsonArrayToFile(collectedItemsJsonArray, collectedItemsFile);
    }

    public static JsonArray loadJsonArrayFromFile(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }


    public static void saveJsonArrayToFile(JsonArray object, File file) {
        try (FileWriter writer = new FileWriter(file)) {
            myGson.toJson(object, writer);
        } catch (IOException e) {
            System.out.println("Could not save json to file!");
            throw new RuntimeException(e);
        }
    }
}

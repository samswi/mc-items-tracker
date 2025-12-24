package com.samswi.itemsTracker;

import com.google.gson.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ItemsTracker implements ModInitializer {

    public static final Object MOD_ID = "itemstracker";
    public static MinecraftServer currentServer;
    public static Path worldFolder;
    public static File collectedItemsFile;

    public static File configFolder;
    public static File blacklistFile;

    public static List<String> lastItems = new ArrayList<>(50);
    public static int lastItemsIterator = 0;

    public static List<String> fullItemsList;
    public static List<String> goalItemsList;
    public static List<String> collectedItemsList;
    public static List<String> remainingItemsList;

    public static List<ServerPlayerEntity> handshakenPlayers;

    // This should be deleted somewhere in the future
    public static JsonArray collectedItemsJsonArray;

    public static List<String> nonNeededItemsList;


    @Override
    public void onInitialize() {
        configFolder = new File(FabricLoader.getInstance().getConfigDir() + "/itemstracker");
        configFolder.mkdirs();
        blacklistFile = new File(configFolder +  "/blacklist.txt");
        fullItemsList = new ArrayList<>(Registries.ITEM.size());
        Set<String> potionEffectsList = new HashSet<>(Registries.POTION.size());
        Registries.POTION.forEach(potion -> potionEffectsList.add(potion.getBaseName()));
        Registries.ITEM.forEach((item) -> {
            if (List.of("minecraft:potion", "minecraft:splash_potion", "minecraft:tipped_arrow", "minecraft:lingering_potion").contains(item.toString())){
                potionEffectsList.forEach(s -> {
                    fullItemsList.add(item + "P" + s);
                });
            }
            else {
                fullItemsList.add(item.toString());
            }
        });
        PayloadTypeRegistry.playS2C().register(NetworkingStuff.OnJoinPayload.ID, NetworkingStuff.OnJoinPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingStuff.RemoveItemPayload.ID, NetworkingStuff.RemoveItemPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingStuff.ShowToastPayload.ID, NetworkingStuff.ShowToastPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkingStuff.HandshakePayload.ID, NetworkingStuff.HandshakePayload.CODEC);

        if (!blacklistFile.exists()){
            try (InputStream in = ItemsTracker.class.getResourceAsStream("/default_blacklist.txt")) {
                Files.copy(in, blacklistFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(NetworkingStuff.HandshakePayload.ID, (handshakePayload, context) -> {
            handshakenPlayers.add(context.player());

        });

    }

    public static void onServerCreation(MinecraftServer server) {
        handshakenPlayers = new ArrayList<>();
        currentServer = server;
        worldFolder = currentServer.getSavePath(WorldSavePath.ROOT).toAbsolutePath();
        collectedItemsFile = worldFolder.resolve(".collected_items.txt").toFile();
        if (!blacklistFile.exists()) {
            try (InputStream in = ItemsTracker.class.getResourceAsStream("/default_blacklist.txt")) {
                Files.copy(in, blacklistFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        List<String> blacklist;
        try {
            blacklist = loadArrayListFromFile(blacklistFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        goalItemsList = new ArrayList<>(fullItemsList);
        blacklist.forEach(string -> {
            if (string.contains("minecraft:")) {
                goalItemsList.removeIf(s -> s.equals(string));
            } else {
                goalItemsList.removeIf(s -> s.contains(string));
            }
        });
        collectedItemsList = new ArrayList<>(goalItemsList.size());
        if (!collectedItemsFile.exists()) {
            try {
                collectedItemsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // This code is for old collected items file format (the json one)
            try (Scanner scanner = new Scanner(collectedItemsFile)) {
                if (scanner.hasNextLine()) {
                    if (scanner.nextLine().startsWith("[")) {
                        collectedItemsJsonArray = loadJsonArrayFromFile(collectedItemsFile);
                        collectedItemsJsonArray.forEach((jsonElement -> collectedItemsList.add(jsonElement.getAsString())));
                    } else {
                        collectedItemsList = loadArrayListFromFile(collectedItemsFile);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        nonNeededItemsList = new ArrayList<>();

        blacklist.forEach(string -> {
            Iterator<String> iterator = collectedItemsList.iterator();
            while (iterator.hasNext()) {
                String item = iterator.next();
                if (string.contains("minecraft:")) {
                    if (item.equals(string)) {
                        nonNeededItemsList.add(item);
                        iterator.remove();
                    }
                } else {
                    if (item.contains(string)) {
                        nonNeededItemsList.add(item);
                        iterator.remove();
                    }
                }
            }
        });
        remainingItemsList = new ArrayList<>(goalItemsList);
        if (collectedItemsList != null) {
            collectedItemsList.forEach(s -> remainingItemsList.remove(s));
        }

    }

    public static void removeItemFromRemainingItems(ItemStack itemStack, PlayerEntity player) {
        String itemId = itemStack.getRegistryEntry().getIdAsString();
        if (itemStack.get(DataComponentTypes.POTION_CONTENTS) != null){
            if (itemStack.get(DataComponentTypes.POTION_CONTENTS).potion().isPresent()) {
                String potionName = itemStack.get(DataComponentTypes.POTION_CONTENTS).potion().get().value().getBaseName();
                itemId = itemId + "P" + potionName;
            }
        }

        if (currentServer.getOverworld().isClient() && currentServer.isDedicated()) return;
        if (!lastItems.contains(itemId)) {
            if (remainingItemsList.remove(itemId)) {
                collectedItemsList.add(itemId);
                Text text = player.getStyledDisplayName().copy().append(Text.of(" obtained ").copyContentOnly().fillStyle(Style.EMPTY).withColor(0xFFAAAAAA)).append(parseItemText(itemId));
                String finalItemId = itemId;
                currentServer.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                    ServerPlayNetworking.send(serverPlayerEntity, new NetworkingStuff.RemoveItemPayload(finalItemId, remainingItemsList.size()));
                    serverPlayerEntity.sendMessage(text);
                });
            } else if (!nonNeededItemsList.contains(itemId)) nonNeededItemsList.add(itemId);
            lastItems.add(lastItemsIterator, itemId);
            lastItemsIterator++;
            if (lastItemsIterator >= 50) lastItemsIterator = 0;
        }
    }

    public static void sendActionBarText(){
        Text actionbarText = Text.literal("")
                .append(Text.literal(String.valueOf(goalItemsList.size() - remainingItemsList.size())).formatted(Formatting.BOLD).withColor(remainingItemsList.isEmpty() ? 0xFF00FF00 : 0xFFFFFFFF))
                .append(Text.literal("/" + goalItemsList.size()).fillStyle(Style.EMPTY).withColor(0xFF888888))
                        .append(Text.literal(" (" + String.format("%.1f%%",(((goalItemsList.size() - remainingItemsList.size()) / (float) goalItemsList.size()))*100) + ")").withColor(remainingItemsList.isEmpty() ? 0xFF00FF00 : 0xFF888888));
        currentServer.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
            if (!handshakenPlayers.contains(serverPlayerEntity) && serverPlayerEntity.age > 20){
                serverPlayerEntity.sendMessage(actionbarText, true);
            }
        });
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
        if (currentServer == null) return;
        if (currentServer.getOverworld().isClient() && currentServer.isDedicated()) return;
        ArrayList<String> itemsToSaveList = new ArrayList<>(collectedItemsList.size() + nonNeededItemsList.size());
        itemsToSaveList.addAll(collectedItemsList);
        itemsToSaveList.addAll(nonNeededItemsList);

        saveCollectionToFile(itemsToSaveList, collectedItemsFile);
    }

    public static JsonArray loadJsonArrayFromFile(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }

    public static ArrayList<String> loadArrayListFromFile(File file) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(file)) {
            ArrayList<String> arrayList = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String string = scanner.nextLine();
                arrayList.add(string);
            }
            return arrayList;
        }
    }

    public static synchronized void saveCollectionToFile(Collection<String> collection, File file) {
        try (FileWriter myWriter = new FileWriter(file)){
            collection.forEach(s -> {
                try {
                    myWriter.write(s + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e){
            System.out.println("Failed to save collectedItemsList");
            currentServer.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                ServerPlayNetworking.send(serverPlayerEntity, new NetworkingStuff.ShowToastPayload("Failed to save .collected_items.txt", "Check server-side logs"));
                serverPlayerEntity.sendMessage(Text.literal("Failed to save .collected_items.txt. Hover me for error info").formatted(Formatting.UNDERLINE).withColor(0xFFFF0000).styled(style -> style.withHoverEvent(new HoverEvent.ShowText(Text.of(e.getMessage())))));

            });
            e.printStackTrace();
        }
    }

    public static ItemStack parseItem(String itemId){
        if (!itemId.contains("P")) return new ItemStack(Registries.ITEM.get(Identifier.of(itemId)));
        ItemStack itemStack = new ItemStack(Registries.ITEM.get(Identifier.of(itemId.split("P")[0])));
        itemStack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(RegistryEntry.of(Registries.POTION.get(Identifier.of(itemId.split("P")[1])))));
        return itemStack;
    }

    public static Text parseItemText(String itemId){
        Text text;
        // This code is a workaround to a problem I was having with parsing Potion items as a hoverable text
        if (itemId.contains("P")){
            text = MutableText.of(Text.of("[").getContent()).append(parseItem(itemId).getFormattedName()).append("]");
        } else {
            text = parseItem(itemId).toHoverableText();
        }
        return text;
    }
}

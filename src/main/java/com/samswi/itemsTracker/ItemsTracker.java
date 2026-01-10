package com.samswi.itemsTracker;

import com.google.gson.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.level.storage.LevelResource;
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

    public static List<ServerPlayer> handshakenPlayers;

    // This should be deleted somewhere in the future
    public static JsonArray collectedItemsJsonArray;

    public static List<String> nonNeededItemsList;
    public static RegistryAccess registryManager;


    @Override
    public void onInitialize() {
        configFolder = new File(FabricLoader.getInstance().getConfigDir() + "/itemstracker");
        configFolder.mkdirs();
        blacklistFile = new File(configFolder +  "/blacklist.txt");

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
        worldFolder = currentServer.getWorldPath(LevelResource.ROOT).toAbsolutePath();
        fullItemsList = new ArrayList<>(BuiltInRegistries.ITEM.size());

        registryManager = server.getAllLevels().iterator().next().registryAccess();

        Set<String> potionEffectsList = new HashSet<>(BuiltInRegistries.POTION.size());
        BuiltInRegistries.POTION.forEach(potion -> potionEffectsList.add(potion.name()));
        Set<String> goatHornInstrumentsList = new HashSet<>(BuiltInRegistries.POTION.size());
        registryManager.lookup(Registries.INSTRUMENT).ifPresent(instruments -> instruments.forEach(instrument -> {
            goatHornInstrumentsList.add(registryManager.lookup(Registries.INSTRUMENT).get().wrapAsHolder(instrument).unwrapKey().get().identifier().toString());
        }));
        registryManager.lookup(Registries.INSTRUMENT).stream();

        BuiltInRegistries.ITEM.forEach((item) -> {
            if (List.of("minecraft:potion", "minecraft:splash_potion", "minecraft:tipped_arrow", "minecraft:lingering_potion").contains(item.toString())){
                potionEffectsList.forEach(s -> {
                    fullItemsList.add(item + "P" + s);
                });
            } else if (item.toString().matches("minecraft:goat_horn")){
                goatHornInstrumentsList.forEach(s -> {
                    fullItemsList.add(item + "I" + s);
                });
            }
            else {
                fullItemsList.add(item.toString());
            }
        });
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

    public static void removeItemFromRemainingItems(ItemStack itemStack, Player player) {
        String itemId = itemStack.getItemHolder().getRegisteredName();
        if (itemStack.get(DataComponents.POTION_CONTENTS) != null){
            if (itemStack.get(DataComponents.POTION_CONTENTS).potion().isPresent()) {
                String potionName = itemStack.get(DataComponents.POTION_CONTENTS).potion().get().value().name();
                itemId = itemId + "P" + potionName;
            }
        }
        if (itemStack.get(DataComponents.INSTRUMENT) != null){
            if (itemStack.get(DataComponents.INSTRUMENT).instrument().key().isPresent()) {
                itemId = itemId + "I" + itemStack.get(DataComponents.INSTRUMENT).instrument().key().get().identifier();
            }
        }

        if (currentServer.overworld().isClientSide() && currentServer.isDedicatedServer()) return;
        if (!lastItems.contains(itemId)) {
            if (remainingItemsList.remove(itemId)) {
                collectedItemsList.add(itemId);
                Component text = player.getFeedbackDisplayName().copy().append(Component.nullToEmpty(" obtained ").plainCopy().withStyle(Style.EMPTY).withColor(0xFFAAAAAA)).append(parseItemText(itemId));
                String finalItemId = itemId;
                currentServer.getPlayerList().getPlayers().forEach(serverPlayerEntity -> {
                    ServerPlayNetworking.send(serverPlayerEntity, new NetworkingStuff.RemoveItemPayload(finalItemId, remainingItemsList.size()));
                    serverPlayerEntity.sendSystemMessage(text);
                });
            } else if (!nonNeededItemsList.contains(itemId)) nonNeededItemsList.add(itemId);
            lastItems.add(lastItemsIterator, itemId);
            lastItemsIterator++;
            if (lastItemsIterator >= 50) lastItemsIterator = 0;
        }
    }

    public static void sendActionBarText(){
        Component actionbarText = Component.literal("")
                .append(Component.literal(String.valueOf(goalItemsList.size() - remainingItemsList.size())).withStyle(ChatFormatting.BOLD).withColor(remainingItemsList.isEmpty() ? 0xFF00FF00 : 0xFFFFFFFF))
                .append(Component.literal("/" + goalItemsList.size()).withStyle(Style.EMPTY).withColor(0xFF888888))
                        .append(Component.literal(" (" + String.format("%.1f%%",(((goalItemsList.size() - remainingItemsList.size()) / (float) goalItemsList.size()))*100) + ")").withColor(remainingItemsList.isEmpty() ? 0xFF00FF00 : 0xFF888888));
        currentServer.getPlayerList().getPlayers().forEach(serverPlayerEntity -> {
            if (!handshakenPlayers.contains(serverPlayerEntity) && serverPlayerEntity.tickCount > 20){
                serverPlayerEntity.displayClientMessage(actionbarText, true);
            }
        });
    }

    public static void onServerExit() {
        if (currentServer.overworld().isClientSide() && currentServer.isDedicatedServer()) return;
        saveItemsToFile();
        remainingItemsList = null;
        lastItems = new ArrayList<>(50);
        lastItemsIterator = 0;
        currentServer = null;
        worldFolder = null;
    }

    public static void saveItemsToFile() {
        if (currentServer == null) return;
        if (currentServer.overworld().isClientSide() && currentServer.isDedicatedServer()) return;
        Set<String> itemsToSaveSet = new HashSet<>(collectedItemsList.size() + nonNeededItemsList.size());
        itemsToSaveSet.addAll(collectedItemsList);
        itemsToSaveSet.addAll(nonNeededItemsList);

        saveCollectionToFile(itemsToSaveSet, collectedItemsFile);
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
            currentServer.getPlayerList().getPlayers().forEach(serverPlayerEntity -> {
                ServerPlayNetworking.send(serverPlayerEntity, new NetworkingStuff.ShowToastPayload("Failed to save .collected_items.txt", "Check server-side logs"));
                serverPlayerEntity.sendSystemMessage(Component.literal("Failed to save .collected_items.txt. Hover me for error info").withStyle(ChatFormatting.UNDERLINE).withColor(0xFFFF0000).withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(e.getMessage())))));

            });
            e.printStackTrace();
        }
    }

    public static ItemStack parseItem(String itemId){
        ItemStack itemStack;
        if (itemId.contains("P")){
            itemStack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId.split("P")[0])));
            itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Holder.direct(BuiltInRegistries.POTION.getValue(Identifier.parse(itemId.split("P")[1])))));
        } else if (itemId.contains("I")){
            itemStack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId.split("I")[0])));

            registryManager.lookup(Registries.INSTRUMENT).ifPresent(instrumentRegistry -> {
                Holder<Instrument> instrumentEntry = instrumentRegistry.get(Identifier.parse(itemId.split("I")[1])).orElse(null);

                if (instrumentEntry != null) {
                    InstrumentComponent instrumentComponent = new InstrumentComponent(instrumentEntry);
                    itemStack.set(DataComponents.INSTRUMENT, instrumentComponent);
                }
            });
        } else {
            itemStack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId)));
        }

        return itemStack;
    }

    public static Component parseItemText(String itemId){
        Component text;
        // This code is a workaround to a problem I was having with parsing Potion items as a hoverable text
        if (itemId.contains("P")) {
            text = MutableComponent.create(Component.nullToEmpty("[").getContents()).append(parseItem(itemId).getStyledHoverName()).append("]");
        } else {
            text = parseItem(itemId).getDisplayName();
        }
        return text;
    }
}

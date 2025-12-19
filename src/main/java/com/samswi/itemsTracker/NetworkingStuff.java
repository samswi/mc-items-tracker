package com.samswi.itemsTracker;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class NetworkingStuff {
    public static final Identifier ON_JOIN_PACKET_ID = Identifier.of("itemstracker", "on_join_packet");
    public static final Identifier REMOVE_ITEM_PACKET_ID = Identifier.of("itemstracker", "remove_item_packet");
    public static final Identifier SHOW_TOAST_PACKET_ID = Identifier.of("itemstracker", "show_toast_packet");

    public record OnJoinPayload(List<String> remainingItems, List<String> goalItems) implements CustomPayload {
        public static final CustomPayload.Id<OnJoinPayload> ID = new CustomPayload.Id<>(ON_JOIN_PACKET_ID);

        private static final PacketCodec<RegistryByteBuf, List<String>> STRING_LIST = new PacketCodec<>() {
            @Override
            public void encode(RegistryByteBuf buf, List<String> list) {
                buf.writeVarInt(list.size());
                for (String s : list) {
                    buf.writeString(s);
                }
            }

            @Override
            public List<String> decode(RegistryByteBuf buf) {
                int size = buf.readVarInt();
                List<String> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    list.add(buf.readString());
                }
                return list;
            }
        };

        public static final PacketCodec<RegistryByteBuf, OnJoinPayload> CODEC = PacketCodec.tuple(
                STRING_LIST, OnJoinPayload::remainingItems,
                STRING_LIST, OnJoinPayload::goalItems,
                OnJoinPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RemoveItemPayload(String itemId, int remainingItemsCount) implements CustomPayload{
        public static final CustomPayload.Id<RemoveItemPayload> ID = new CustomPayload.Id<>(REMOVE_ITEM_PACKET_ID);
        public static final PacketCodec<RegistryByteBuf, RemoveItemPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, RemoveItemPayload::itemId,
                PacketCodecs.INTEGER, RemoveItemPayload::remainingItemsCount,
                RemoveItemPayload::new
        );
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record ShowToastPayload(String title, String description) implements CustomPayload{
        public static final CustomPayload.Id<ShowToastPayload> ID = new CustomPayload.Id<>(SHOW_TOAST_PACKET_ID);
        public static final PacketCodec<RegistryByteBuf, ShowToastPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, ShowToastPayload::title,
                PacketCodecs.STRING, ShowToastPayload::description,
                ShowToastPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {return ID;}
    }
}



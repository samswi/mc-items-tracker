package com.samswi.itemsTracker;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class NetworkingStuff {
    public static final Identifier ON_JOIN_PACKET_ID = Identifier.fromNamespaceAndPath("itemstracker", "on_join_packet");
    public static final Identifier REMOVE_ITEM_PACKET_ID = Identifier.fromNamespaceAndPath("itemstracker", "remove_item_packet");
    public static final Identifier SHOW_TOAST_PACKET_ID = Identifier.fromNamespaceAndPath("itemstracker", "show_toast_packet");
    public static final Identifier HANDSHAKE_PACKET_ID = Identifier.fromNamespaceAndPath("itemstracker", "handshake");

    public record OnJoinPayload(List<String> remainingItems, List<String> goalItems) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OnJoinPayload> ID = new CustomPacketPayload.Type<>(ON_JOIN_PACKET_ID);

        private static final StreamCodec<RegistryFriendlyByteBuf, List<String>> STRING_LIST = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, List<String> list) {
                buf.writeVarInt(list.size());
                for (String s : list) {
                    buf.writeUtf(s);
                }
            }

            @Override
            public List<String> decode(RegistryFriendlyByteBuf buf) {
                int size = buf.readVarInt();
                List<String> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    list.add(buf.readUtf());
                }
                return list;
            }
        };

        public static final StreamCodec<RegistryFriendlyByteBuf, OnJoinPayload> CODEC = StreamCodec.composite(
                STRING_LIST, OnJoinPayload::remainingItems,
                STRING_LIST, OnJoinPayload::goalItems,
                OnJoinPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record RemoveItemPayload(String itemId, int remainingItemsCount) implements CustomPacketPayload{
        public static final CustomPacketPayload.Type<RemoveItemPayload> ID = new CustomPacketPayload.Type<>(REMOVE_ITEM_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, RemoveItemPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, RemoveItemPayload::itemId,
                ByteBufCodecs.INT, RemoveItemPayload::remainingItemsCount,
                RemoveItemPayload::new
        );
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record ShowToastPayload(String title, String description) implements CustomPacketPayload{
        public static final CustomPacketPayload.Type<ShowToastPayload> ID = new CustomPacketPayload.Type<>(SHOW_TOAST_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ShowToastPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ShowToastPayload::title,
                ByteBufCodecs.STRING_UTF8, ShowToastPayload::description,
                ShowToastPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {return ID;}
    }

    public record HandshakePayload() implements CustomPacketPayload{
        public static final CustomPacketPayload.Type<HandshakePayload> ID = new CustomPacketPayload.Type<>(HANDSHAKE_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, HandshakePayload> CODEC = StreamCodec.unit(new HandshakePayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {return ID;}
    }
}



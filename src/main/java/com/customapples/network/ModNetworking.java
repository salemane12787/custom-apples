package com.customapples.network;

import com.customapples.CustomApplesMod;
import com.customapples.client.ClientShakeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class ModNetworking {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CustomApplesMod.loc("main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    private static int id = 0;

    private ModNetworking() {}

    public static void register() {
        CHANNEL.registerMessage(id++, ShakePacket.class, ShakePacket::encode, ShakePacket::decode, ShakePacket::handle);
        CHANNEL.registerMessage(id++, WormNamePacket.class, WormNamePacket::encode, WormNamePacket::decode, WormNamePacket::handle);
        CHANNEL.registerMessage(id++, OpenWormNamePacket.class, OpenWormNamePacket::encode, OpenWormNamePacket::decode, OpenWormNamePacket::handle);
        CHANNEL.registerMessage(id++, FlashPacket.class, FlashPacket::encode, FlashPacket::decode, FlashPacket::handle);
        CHANNEL.registerMessage(id++, EntityScalePacket.class, EntityScalePacket::encode, EntityScalePacket::decode, EntityScalePacket::handle);
    }

    public record ShakePacket(float intensity) {
        public static void encode(ShakePacket pkt, FriendlyByteBuf buf) {
            buf.writeFloat(pkt.intensity);
        }

        public static ShakePacket decode(FriendlyByteBuf buf) {
            return new ShakePacket(buf.readFloat());
        }

        public static void handle(ShakePacket pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> ClientShakeHandler.trigger(pkt.intensity())));
            ctx.get().setPacketHandled(true);
        }
    }

    public record WormNamePacket(int entityId, String name) {
        public static void encode(WormNamePacket pkt, FriendlyByteBuf buf) {
            buf.writeVarInt(pkt.entityId);
            buf.writeUtf(pkt.name);
        }

        public static WormNamePacket decode(FriendlyByteBuf buf) {
            return new WormNamePacket(buf.readVarInt(), buf.readUtf());
        }

        public static void handle(WormNamePacket pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null) return;
                var entity = player.level().getEntity(pkt.entityId);
                if (entity instanceof com.customapples.entity.WormEntity worm) {
                    worm.setCustomName(net.minecraft.network.chat.Component.literal(pkt.name));
                    worm.setCustomNameVisible(true);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public record OpenWormNamePacket(int entityId) {
        public static void encode(OpenWormNamePacket pkt, FriendlyByteBuf buf) {
            buf.writeVarInt(pkt.entityId);
        }

        public static OpenWormNamePacket decode(FriendlyByteBuf buf) {
            return new OpenWormNamePacket(buf.readVarInt());
        }

        public static void handle(OpenWormNamePacket pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> com.customapples.client.ClientWormHandler.openNameScreen(pkt.entityId())));
            ctx.get().setPacketHandled(true);
        }
    }

    public record FlashPacket(int ticks) {
        public static void encode(FlashPacket pkt, FriendlyByteBuf buf) {
            buf.writeVarInt(pkt.ticks);
        }

        public static FlashPacket decode(FriendlyByteBuf buf) {
            return new FlashPacket(buf.readVarInt());
        }

        public static void handle(FlashPacket pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> com.customapples.client.ClientFlashHandler.trigger(pkt.ticks())));
            ctx.get().setPacketHandled(true);
        }
    }

    public static void sendShake(ServerPlayer player, float intensity) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ShakePacket(intensity));
    }

    public static void sendFlash(ServerPlayer player, int ticks) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new FlashPacket(ticks));
    }

    public static void sendOpenWormName(ServerPlayer player, int entityId) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenWormNamePacket(entityId));
    }

    public static void sendWormName(int entityId, String name) {
        CHANNEL.sendToServer(new WormNamePacket(entityId, name));
    }

    public record EntityScalePacket(int entityId, float scale) {
        public static void encode(EntityScalePacket pkt, FriendlyByteBuf buf) {
            buf.writeVarInt(pkt.entityId);
            buf.writeFloat(pkt.scale);
        }

        public static EntityScalePacket decode(FriendlyByteBuf buf) {
            return new EntityScalePacket(buf.readVarInt(), buf.readFloat());
        }

        public static void handle(EntityScalePacket pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> com.customapples.client.ClientEntityScaleCache.set(pkt.entityId, pkt.scale)));
            ctx.get().setPacketHandled(true);
        }
    }

    public static void sendEntityScale(LivingEntity entity, float scale) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new EntityScalePacket(entity.getId(), scale));
    }

    public static void sendEntityScaleClear(LivingEntity entity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new EntityScalePacket(entity.getId(), 1.0f));
    }
}

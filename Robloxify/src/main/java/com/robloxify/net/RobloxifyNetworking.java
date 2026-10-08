package com.robloxify.net;

import com.robloxify.server.RobloxifyService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Payload registration plus small helpers for sending packets. */
public final class RobloxifyNetworking {
	private RobloxifyNetworking() {
	}

	public static void registerPayloads() {
		PayloadTypeRegistry.clientboundPlay().register(RobloxifyPayloads.Profile.TYPE, RobloxifyPayloads.Profile.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(RobloxifyPayloads.Appearance.TYPE, RobloxifyPayloads.Appearance.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(RobloxifyPayloads.Notification.TYPE, RobloxifyPayloads.Notification.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(RobloxifyPayloads.Emote.TYPE, RobloxifyPayloads.Emote.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(RobloxifyPayloads.Experience.TYPE, RobloxifyPayloads.Experience.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RobloxifyPayloads.Action.TYPE, RobloxifyPayloads.Action.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(RobloxifyPayloads.Action.TYPE, (payload, context) ->
				context.server().execute(() -> RobloxifyService.handleAction(context.player(), payload.action(), payload.arg())));
	}

	public static void sendTo(ServerPlayer player, CustomPacketPayload payload) {
		if (ServerPlayNetworking.canSend(player, payload.type())) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	public static void broadcast(MinecraftServer server, CustomPacketPayload payload) {
		if (server == null) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			sendTo(player, payload);
		}
	}
}

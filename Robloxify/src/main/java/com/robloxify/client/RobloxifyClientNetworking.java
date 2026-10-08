package com.robloxify.client;

import com.robloxify.net.RobloxifyPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Client side packet receivers. */
public final class RobloxifyClientNetworking {
	private RobloxifyClientNetworking() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(RobloxifyPayloads.Profile.TYPE,
				(payload, context) -> ClientRobloxState.applyProfile(payload));
		ClientPlayNetworking.registerGlobalReceiver(RobloxifyPayloads.Avatar.TYPE,
				(payload, context) -> ClientRobloxState.setAvatar(payload.player(), payload.roblox()));
		ClientPlayNetworking.registerGlobalReceiver(RobloxifyPayloads.Emote.TYPE,
				(payload, context) -> ClientRobloxState.playEmote(payload.player(), payload.emote()));
		ClientPlayNetworking.registerGlobalReceiver(RobloxifyPayloads.BadgeUnlocked.TYPE,
				(payload, context) -> ClientRobloxState.onBadgeUnlocked(payload.badgeId(), payload.robuxReward()));
		ClientPlayNetworking.registerGlobalReceiver(RobloxifyPayloads.Obby.TYPE,
				(payload, context) -> ClientRobloxState.applyObby(payload));
	}

	/** Sends a UI action to the server. Safe to call when playing on a server without the mod. */
	public static void send(String action, String arg) {
		if (ClientPlayNetworking.canSend(RobloxifyPayloads.Action.TYPE)) {
			ClientPlayNetworking.send(new RobloxifyPayloads.Action(action, arg));
		}
	}
}

package com.robloxify.client;

import com.robloxify.client.notification.NotificationManager;
import com.robloxify.net.RobloxifyPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Client side packet receivers. */
public final class RobloxifyClientNetworking {
	private RobloxifyClientNetworking() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(RobloxifyPayloads.Profile.TYPE,
				(payload, context) -> ClientRobloxState.applyProfile(payload));
		ClientPlayNetworking.registerGlobalReceiver(RobloxifyPayloads.Appearance.TYPE,
				(payload, context) -> ClientRobloxState.applyAppearance(payload));
		ClientPlayNetworking.registerGlobalReceiver(RobloxifyPayloads.Notification.TYPE,
				(payload, context) -> NotificationManager.push(payload));
		ClientPlayNetworking.registerGlobalReceiver(RobloxifyPayloads.Emote.TYPE,
				(payload, context) -> ClientRobloxState.playEmote(payload.player(), payload.emote()));
		ClientPlayNetworking.registerGlobalReceiver(RobloxifyPayloads.Experience.TYPE,
				(payload, context) -> ClientRobloxState.applyExperience(payload));
	}

	/** Sends a UI action to the server. Safe to call when the server does not have the mod. */
	public static void send(String action, String arg) {
		if (ClientPlayNetworking.canSend(RobloxifyPayloads.Action.TYPE)) {
			ClientPlayNetworking.send(new RobloxifyPayloads.Action(action, arg));
		}
	}
}

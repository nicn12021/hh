package com.robloxify;

import com.robloxify.client.ClientRobloxState;
import com.robloxify.client.RobloxAvatarEffects;
import com.robloxify.client.RobloxifyClientNetworking;
import com.robloxify.client.RobloxifyKeybinds;
import com.robloxify.client.avatar.RobloxAvatarModel;
import com.robloxify.client.hud.RobloxHud;
import com.robloxify.client.notification.NotificationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

/** Client-only entrypoint of Robloxify. */
public class RobloxifyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		RobloxifyClientNetworking.register();
		RobloxifyKeybinds.register();

		HudElementRegistry.addLast(Robloxify.id("hud"), new RobloxHud());
		ModelLayerRegistry.registerModelLayer(RobloxAvatarModel.LAYER, RobloxAvatarModel::createLayer);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientRobloxState.tick();
			NotificationManager.tick();
			RobloxifyKeybinds.tick(client);
			RobloxAvatarEffects.tick(client);
		});

		Robloxify.LOGGER.info("Robloxify client initialized!");
	}
}

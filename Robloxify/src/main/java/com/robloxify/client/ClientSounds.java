package com.robloxify.client;

import com.robloxify.config.RobloxifyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

/** Client side UI sound helper. */
public final class ClientSounds {
	private ClientSounds() {
	}

	public static void play(SoundEvent event, float pitch) {
		if (!RobloxifyConfig.get().robloxSounds) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.getSoundManager() == null) {
			return;
		}
		client.getSoundManager().play(SimpleSoundInstance.forUI(event, pitch));
	}
}

package com.robloxify.client;

import com.robloxify.Robloxify;
import com.robloxify.client.ui.RobloxHomeScreen;
import com.robloxify.config.RobloxifyConfig;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

/** Client key bindings. */
public final class RobloxifyKeybinds {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Robloxify.id("main"));

	public static KeyMapping openMenu;
	public static KeyMapping toggleAvatar;
	public static KeyMapping wave;

	private RobloxifyKeybinds() {
	}

	public static void register() {
		openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.robloxify.menu",
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));
		toggleAvatar = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.robloxify.avatar",
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));
		wave = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.robloxify.wave",
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY));
	}

	public static void tick(Minecraft client) {
		while (openMenu.consumeClick()) {
			if (client.gui.screen() == null && client.player != null && RobloxifyConfig.get().robloxUi) {
				ClientSounds.play(com.robloxify.sound.RobloxifySounds.UI_OPEN, 1.0F);
				client.gui.setScreen(new RobloxHomeScreen());
			}
		}
		while (toggleAvatar.consumeClick()) {
			if (client.player != null) {
				RobloxifyClientNetworking.send("avatar_toggle", "");
			}
		}
		while (wave.consumeClick()) {
			if (client.player != null && RobloxifyConfig.get().emotes) {
				RobloxifyClientNetworking.send("emote", "wave");
			}
		}
	}
}

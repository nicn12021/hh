package com.robloxify.client.ui;

import com.robloxify.client.ClientRobloxState;
import com.robloxify.client.RobloxifyClientNetworking;
import com.robloxify.config.RobloxifyConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Avatar tab - the main Robloxify hub. */
public class RobloxMainScreen extends RobloxScreen {
	private static final String[] EMOTES = {"wave", "dance", "point", "idle", "walk", "run", "jump", "fall"};

	public RobloxMainScreen() {
		super(Component.literal("Robloxify"));
	}

	@Override
	protected RobloxTab currentTab() {
		return RobloxTab.AVATAR;
	}

	@Override
	protected void init() {
		super.init();
		int centerX = this.width / 2;

		addRenderableWidget(Button.builder(avatarLabel(), button -> {
			RobloxifyClientNetworking.send("avatar_toggle", "");
			button.setMessage(avatarLabel());
		}).bounds(centerX - 100, 58, 200, 20).build());

		addRenderableWidget(Button.builder(Component.literal("Start Obby"), button -> {
			RobloxifyClientNetworking.send("obby_start", "");
			this.minecraft.gui.setScreen(null);
		}).bounds(centerX - 100, 82, 98, 20).build());

		addRenderableWidget(Button.builder(Component.literal("Stop Obby"), button ->
			RobloxifyClientNetworking.send("obby_stop", ""))
			.bounds(centerX + 2, 82, 98, 20).build());

		int columns = 4;
		int buttonWidth = 96;
		int startX = centerX - (columns * (buttonWidth + 4)) / 2;
		for (int i = 0; i < EMOTES.length; i++) {
			String emote = EMOTES[i];
			int x = startX + (i % columns) * (buttonWidth + 4);
			int y = 116 + (i / columns) * 24;
			addRenderableWidget(Button.builder(Component.literal(capitalise(emote)), button ->
					RobloxifyClientNetworking.send("emote", emote))
					.bounds(x, y, buttonWidth, 20).build());
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int centerX = this.width / 2;
		graphics.centeredText(this.font, "Current look: " + (ClientRobloxState.selfAvatar() ? "Roblox avatar" : "Minecraft player"),
				centerX, 40, RobloxTheme.ACCENT);
		graphics.centeredText(this.font, "Emotes (also /robloxify emote <name>)", centerX, 104, RobloxTheme.TEXT_DIM);
		if (!RobloxifyConfig.get().robloxAvatar) {
			graphics.centeredText(this.font, "Roblox avatar disabled in Settings", centerX, this.height - 16, 0xFFFF5555);
		}
	}

	private static Component avatarLabel() {
		return Component.literal(ClientRobloxState.selfAvatar() ? "Switch to Minecraft Player" : "Switch to Roblox Avatar");
	}

	private static String capitalise(String value) {
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}

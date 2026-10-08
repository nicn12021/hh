package com.robloxify.client.ui;

import com.robloxify.config.RobloxifyConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

/** Settings tab: every Robloxify feature can be toggled here. */
public class RobloxSettingsScreen extends RobloxScreen {
	private static final List<String> KEYS = List.of(
			"roblox_avatar", "roblox_hud", "robux", "badges", "experiences",
			"roblox_sounds", "emotes", "roblox_ui", "easter_eggs");

	public RobloxSettingsScreen() {
		super(Component.literal("Settings"));
	}

	@Override
	protected RobloxTab currentTab() {
		return RobloxTab.SETTINGS;
	}

	@Override
	protected void init() {
		super.init();
		int y = 52;
		for (String key : KEYS) {
			addRenderableWidget(Button.builder(label(key), button -> {
				Map<String, Boolean> toggles = RobloxifyConfig.get().toggles();
				boolean next = !toggles.getOrDefault(key, true);
				RobloxifyConfig.get().setToggle(key, next);
				button.setMessage(label(key));
			}).bounds(this.width - 96, y, 84, 18).build());
			y += 22;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		Map<String, Boolean> toggles = RobloxifyConfig.get().toggles();
		int y = 52;
		for (String key : KEYS) {
			graphics.text(this.font, pretty(key), 16, y + 5, RobloxTheme.TEXT);
			y += 22;
		}
		graphics.text(this.font, "Config file: config/robloxify.json", 16, this.height - 20, RobloxTheme.TEXT_DIM);
	}

	private static Component label(String key) {
		return Component.literal(RobloxifyConfig.get().toggles().getOrDefault(key, true) ? "ON" : "OFF");
	}

	private static String pretty(String key) {
		return key.replace('_', ' ');
	}
}

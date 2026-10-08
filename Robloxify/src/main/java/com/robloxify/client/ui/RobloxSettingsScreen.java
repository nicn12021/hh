package com.robloxify.client.ui;

import com.robloxify.config.RobloxifyConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Every Robloxify feature can be toggled here. */
public class RobloxSettingsScreen extends RobloxScreen {
	private static final List<String> KEYS = List.of(
			"roblox_avatar", "avatar_animations", "roblox_physics", "roblox_hud", "show_robux",
			"show_badges", "notifications", "experiences", "badges", "robux", "roblox_ui",
			"roblox_sounds", "emotes", "easter_eggs");

	private static final int ROW_HEIGHT = 20;
	private static final int ROW_GAP = 3;

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
		int x = CONTENT_X;
		int width = contentWidth();
		int columnWidth = (width - 8) / 2;
		for (int i = 0; i < KEYS.size(); i++) {
			String key = KEYS.get(i);
			int cx = x + (i % 2) * (columnWidth + 8);
			int cy = CONTENT_Y + 18 + (i / 2) * (ROW_HEIGHT + ROW_GAP);
			addRenderableWidget(new RobloxButton(cx + columnWidth - 54, cy, 52, ROW_HEIGHT,
					Component.literal(on(key) ? "ON" : "OFF"), () -> {
					RobloxifyConfig.get().setToggle(key, !on(key));
					this.rebuildWidgets();
				}).accent(on(key) ? RobloxTheme.ACCENT : RobloxTheme.RED));
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int x = CONTENT_X;
		int width = contentWidth();
		int columnWidth = (width - 8) / 2;
		graphics.text(this.font, "FEATURES", x, CONTENT_Y, RobloxTheme.TEXT_DIM);

		for (int i = 0; i < KEYS.size(); i++) {
			String key = KEYS.get(i);
			int cx = x + (i % 2) * (columnWidth + 8);
			int cy = CONTENT_Y + 18 + (i / 2) * (ROW_HEIGHT + ROW_GAP);
			graphics.fill(cx, cy, cx + columnWidth, cy + ROW_HEIGHT, RobloxTheme.PANEL);
			graphics.fill(cx, cy, cx + 2, cy + ROW_HEIGHT, on(key) ? RobloxTheme.ACCENT : RobloxTheme.BORDER);
			graphics.text(this.font, key.replace('_', ' '), cx + 8, cy + 6, RobloxTheme.TEXT);
		}
		int footer = CONTENT_Y + 18 + ((KEYS.size() + 1) / 2) * (ROW_HEIGHT + ROW_GAP) + 6;
		graphics.text(this.font, "Config file: config/robloxify.json   -   /robloxify config <key> <value>",
				x, footer, RobloxTheme.TEXT_MUTED);
	}

	private static boolean on(String key) {
		return RobloxifyConfig.get().toggles().getOrDefault(key, true);
	}
}

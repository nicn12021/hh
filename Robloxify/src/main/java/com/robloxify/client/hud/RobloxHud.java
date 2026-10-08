package com.robloxify.client.hud;

import com.robloxify.client.ClientRobloxState;
import com.robloxify.config.RobloxifyConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * The Roblox HUD: a small Robux chip, the obby panel and the badge banner.
 * Everything is drawn with plain rectangles and text, so it is cheap and scales with the GUI scale.
 */
public final class RobloxHud implements HudElement {
	private static final int CHIP_BG = 0xB0121016;
	private static final int CHIP_ACCENT = 0xFF00B06A;
	private static final int PANEL_BG = 0xC0121016;
	private static final int TEXT = 0xFFF2F2F2;
	private static final int TEXT_DIM = 0xFFA8A8B0;

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.font == null) {
			return;
		}
		RobloxifyConfig config = RobloxifyConfig.get();
		Font font = client.font;

		if (config.robloxHud) {
			renderRobuxChip(graphics, font, config);
		}
		if (config.experiences && ClientRobloxState.obbyActive()) {
			renderObbyPanel(graphics, font, config);
		}
		if (ClientRobloxState.hasBanner()) {
			renderBanner(graphics, font);
		}
	}

	private void renderRobuxChip(GuiGraphicsExtractor graphics, Font font, RobloxifyConfig config) {
		String text = "\u25C8 " + ClientRobloxState.formatNumber(ClientRobloxState.robux());
		int width = font.width(text) + 13;
		int x = config.hudOffsetX;
		int y = config.hudOffsetY;
		graphics.fill(x, y, x + width, y + 14, CHIP_BG);
		graphics.fill(x, y, x + 2, y + 14, CHIP_ACCENT);
		graphics.text(font, text, x + 7, y + 3, TEXT);
	}

	private void renderObbyPanel(GuiGraphicsExtractor graphics, Font font, RobloxifyConfig config) {
		long millis = ClientRobloxState.obbyElapsedMillis();
		String title = "OBBY  " + ClientRobloxState.obbyCheckpoint() + "/" + ClientRobloxState.obbyTotal();
		String time = String.format(java.util.Locale.US, "%.1fs", millis / 1000.0);
		int width = Math.max(font.width(title), font.width(time)) + 14;
		int x = config.hudOffsetX;
		int y = config.hudOffsetY + 18;
		graphics.fill(x, y, x + width, y + 26, PANEL_BG);
		graphics.fill(x, y, x + width, y + 2, 0xFFFFC64A);
		graphics.text(font, title, x + 7, y + 5, TEXT);
		graphics.text(font, time, x + 7, y + 15, TEXT_DIM);
	}

	private void renderBanner(GuiGraphicsExtractor graphics, Font font) {
		String title = ClientRobloxState.bannerTitle();
		String subtitle = ClientRobloxState.bannerSubtitle();
		int center = graphics.guiWidth() / 2;
		int width = Math.max(font.width(title), font.width(subtitle)) + 24;
		int x = center - width / 2;
		int y = 10;
		graphics.fill(x, y, x + width, y + 32, 0xD8121016);
		graphics.fill(x, y, x + width, y + 2, ClientRobloxState.bannerColor());
		graphics.centeredText(font, title, center, y + 7, TEXT);
		graphics.centeredText(font, subtitle, center, y + 19, TEXT_DIM);
	}
}

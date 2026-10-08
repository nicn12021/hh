package com.robloxify.client.ui;

import com.robloxify.util.Robux;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Small drawing helpers shared by every Robloxify screen. All flat rectangles, all cheap. */
public final class RobloxUi {
	private RobloxUi() {
	}

	public static void panel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		panel(graphics, x, y, width, height, RobloxTheme.PANEL);
	}

	public static void panel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int fill) {
		graphics.fill(x, y, x + width, y + height, fill);
		graphics.fill(x, y, x + width, y + 1, 0x18FFFFFF);
		graphics.fill(x, y + height - 1, x + width, y + height, 0x40000000);
	}

	public static void card(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int accent, boolean highlighted) {
		graphics.fill(x, y, x + width, y + height, highlighted ? RobloxTheme.PANEL_HOVER : RobloxTheme.PANEL);
		graphics.fill(x, y, x + 3, y + height, accent);
		if (highlighted) {
			graphics.fill(x, y, x + width, y + 1, accent);
		}
	}

	public static void chip(GuiGraphicsExtractor graphics, Font font, int x, int y, String text, int color) {
		int width = font.width(text) + 10;
		graphics.fill(x, y, x + width, y + 13, 0xFF20202A);
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.text(font, text, x + 5, y + 3, color);
	}

	/** The Robux balance chip, with a drawn currency mark. */
	public static int robuxChip(GuiGraphicsExtractor graphics, Font font, int rightX, int y, int amount) {
		String text = Robux.plain(amount);
		int width = font.width(text) + 26;
		int x = rightX - width;
		graphics.fill(x, y, x + width, y + 15, 0xFF20202A);
		graphics.fill(x, y, x + width, y + 2, RobloxTheme.GOLD);
		robuxIcon(graphics, x + 5, y + 4, 7, RobloxTheme.GOLD);
		graphics.text(font, text, x + 17, y + 4, RobloxTheme.TEXT);
		return width;
	}

	/** Draws a compact R$ mark out of rectangles. */
	public static void robuxIcon(GuiGraphicsExtractor graphics, int x, int y, int size, int color) {
		int unit = Math.max(1, size / 7);
		rect(graphics, x, y, unit, size, color);
		rect(graphics, x, y, size - unit, unit, color);
		rect(graphics, x + size - unit, y, unit, unit * 3, color);
		rect(graphics, x, y + unit * 3, size - unit, unit, color);
		rect(graphics, x + unit * 3, y + unit * 3, unit, unit, color);
		rect(graphics, x + unit * 4, y + unit * 4, unit, unit, color);
		rect(graphics, x + unit * 5, y + unit * 5, unit, unit * 2, color);
	}

	private static void rect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + height, color);
	}

	public static void progressBar(GuiGraphicsExtractor graphics, int x, int y, int width, int height, float progress, int color) {
		graphics.fill(x, y, x + width, y + height, 0xFF101018);
		int filled = (int) (width * Math.max(0.0F, Math.min(1.0F, progress)));
		graphics.fill(x, y, x + filled, y + height, color);
	}

	/** A drawn badge rosette. */
	public static void badgeIcon(GuiGraphicsExtractor graphics, int x, int y, int size, int color) {
		graphics.fill(x, y, x + size, y + size, 0xFF101018);
		graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, color);
		graphics.fill(x + 2, y + 2, x + size - 2, y + size - 2, 0xFF101018);
		graphics.fill(x + 3, y + 3, x + size - 3, y + size - 3, color);
	}

	public static void divider(GuiGraphicsExtractor graphics, int x, int y, int width) {
		graphics.fill(x, y, x + width, y + 1, RobloxTheme.BORDER);
	}

	public static void label(GuiGraphicsExtractor graphics, Font font, int x, int y, String text, int color) {
		graphics.text(font, text, x, y, color);
	}
}

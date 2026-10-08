package com.robloxify.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** A flat, chunky, Roblox style button. Replaces the vanilla widget look entirely. */
public class RobloxButton extends AbstractWidget {
	private final Runnable action;
	private int accent = RobloxTheme.ACCENT;
	private String icon = "";
	private String trailing = "";
	private boolean primary;
	private boolean leftAligned;

	public RobloxButton(int x, int y, int width, int height, Component message, Runnable action) {
		super(x, y, width, height, message);
		this.action = action;
	}

	public RobloxButton accent(int color) {
		this.accent = color;
		return this;
	}

	public RobloxButton icon(String glyph) {
		this.icon = glyph;
		return this;
	}

	public RobloxButton trailing(String text) {
		this.trailing = text;
		return this;
	}

	public RobloxButton primary() {
		this.primary = true;
		return this;
	}

	public RobloxButton leftAligned() {
		this.leftAligned = true;
		return this;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		int x = getX();
		int y = getY();
		int width = getWidth();
		int height = getHeight();
		boolean hovered = isHoveredOrFocused() && active;

		int fill = !active ? RobloxTheme.PANEL_DISABLED
				: hovered ? RobloxTheme.PANEL_HOVER
				: primary ? RobloxTheme.PANEL_ALT : RobloxTheme.PANEL;
		graphics.fill(x, y, x + width, y + height, fill);
		graphics.fill(x, y, x + width, y + 2, active ? accent : RobloxTheme.BORDER);
		graphics.fill(x, y + height - 1, x + width, y + height, 0x50000000);
		if (hovered) {
			graphics.fill(x, y, x + width, y + height, 0x14FFFFFF);
		}

		Font font = Minecraft.getInstance().font;
		int textColor = active ? RobloxTheme.TEXT : RobloxTheme.TEXT_MUTED;
		String label = getMessage().getString();
		int textX = x + (icon.isEmpty() ? 8 : 22);

		if (!icon.isEmpty()) {
			graphics.text(font, icon, x + 8, y + (height - 8) / 2, accent);
		}
		if (!leftAligned) {
			textX = x + (width - font.width(label)) / 2;
		}
		graphics.text(font, label, textX, y + (height - 8) / 2, textColor);

		if (!trailing.isEmpty()) {
			graphics.text(font, trailing, x + width - 8 - font.width(trailing), y + (height - 8) / 2,
					active ? RobloxTheme.GOLD : RobloxTheme.TEXT_MUTED);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		if (!active) {
			return;
		}
		action.run();
		playDownSound(Minecraft.getInstance().getSoundManager());
	}
}

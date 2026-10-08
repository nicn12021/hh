package com.robloxify.client.hud;

import com.robloxify.client.ClientRobloxState;
import com.robloxify.client.notification.NotificationManager;
import com.robloxify.client.ui.RobloxTheme;
import com.robloxify.client.ui.RobloxUi;
import com.robloxify.config.RobloxifyConfig;
import com.robloxify.experience.ExperienceType;
import com.robloxify.util.Robux;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * The Robloxify HUD: a Robux chip, the Experience panel and the notification stack.
 * Everything is flat rectangles and text, so it costs nothing while idle.
 */
public final class RobloxHud implements HudElement {
	private static final int CHIP_BG = 0xD914141C;

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.font == null) {
			return;
		}
		RobloxifyConfig config = RobloxifyConfig.get();
		Font font = client.font;

		if (config.robloxHud && config.showRobux) {
			renderRobuxChip(graphics, font, config);
		}
		if (config.experiences && ClientRobloxState.experienceActive()) {
			renderExperiencePanel(graphics, font, config);
		}
		if (config.notifications) {
			NotificationManager.render(graphics, font, graphics.guiWidth());
		}
	}

	private void renderRobuxChip(GuiGraphicsExtractor graphics, Font font, RobloxifyConfig config) {
		String text = Robux.compact(ClientRobloxState.robux());
		int width = font.width(text) + 26;
		int x = config.hudOffsetX;
		int y = config.hudOffsetY;
		graphics.fill(x, y, x + width, y + 15, CHIP_BG);
		graphics.fill(x, y, x + width, y + 2, RobloxTheme.GOLD);
		RobloxUi.robuxIcon(graphics, x + 5, y + 4, 7, RobloxTheme.GOLD);
		graphics.text(font, text, x + 17, y + 4, RobloxTheme.TEXT);
	}

	private void renderExperiencePanel(GuiGraphicsExtractor graphics, Font font, RobloxifyConfig config) {
		ExperienceType type = ClientRobloxState.experienceType();
		String title = type != null ? type.displayName().toUpperCase() : "EXPERIENCE";
		String objective = ClientRobloxState.objective();
		long millis = ClientRobloxState.elapsedMillis();
		String time = String.format(java.util.Locale.US, "%.1fs", millis / 1000.0);

		int width = Math.max(font.width(objective), font.width(title) + font.width(time) + 24) + 20;
		int x = config.hudOffsetX;
		int y = config.hudOffsetY + 19;
		int accent = type != null ? type.accent() : RobloxTheme.ACCENT;

		graphics.fill(x, y, x + width, y + 42, CHIP_BG);
		graphics.fill(x, y, x + width, y + 2, accent);
		graphics.text(font, title, x + 8, y + 7, accent);
		graphics.text(font, time, x + width - 8 - font.width(time), y + 7, RobloxTheme.TEXT_DIM);
		graphics.text(font, objective, x + 8, y + 19, RobloxTheme.TEXT);

		int total = Math.max(1, ClientRobloxState.checkpointTotal());
		float progress = ClientRobloxState.checkpoint() / (float) total;
		RobloxUi.progressBar(graphics, x + 8, y + 32, width - 16, 4, progress, accent);
	}
}

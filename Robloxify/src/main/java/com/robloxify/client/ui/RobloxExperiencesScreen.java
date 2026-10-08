package com.robloxify.client.ui;

import com.robloxify.client.ClientRobloxState;
import com.robloxify.client.RobloxifyClientNetworking;
import com.robloxify.experience.ExperienceType;
import com.robloxify.util.Robux;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** The Experience selector. */
public class RobloxExperiencesScreen extends RobloxScreen {
	private static final int CARD_HEIGHT = 54;
	private static final int CARD_GAP = 6;

	public RobloxExperiencesScreen() {
		super(Component.literal("Experiences"));
	}

	@Override
	protected RobloxTab currentTab() {
		return RobloxTab.EXPERIENCES;
	}

	@Override
	protected void init() {
		super.init();
		int x = CONTENT_X;
		int width = contentWidth();
		int columnWidth = (width - CARD_GAP) / 2;
		ExperienceType[] types = ExperienceType.values();
		for (int i = 0; i < types.length; i++) {
			ExperienceType type = types[i];
			int cx = x + (i % 2) * (columnWidth + CARD_GAP);
			int cy = CONTENT_Y + 22 + (i / 2) * (CARD_HEIGHT + CARD_GAP);
			addRenderableWidget(new RobloxButton(cx + columnWidth - 62, cy + 8, 56, 18,
					Component.literal("PLAY"), () -> {
					RobloxifyClientNetworking.send("experience_start", type.id());
					this.minecraft.gui.setScreen(null);
				}).accent(type.accent()).primary());
		}
		int y = CONTENT_Y + 22 + ((types.length + 1) / 2) * (CARD_HEIGHT + CARD_GAP) + 4;
		addRenderableWidget(new RobloxButton(x, y, 120, 20, Component.literal("Leave Experience"),
				() -> RobloxifyClientNetworking.send("experience_stop", ""))
				.accent(RobloxTheme.RED));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int x = CONTENT_X;
		int width = contentWidth();
		int columnWidth = (width - CARD_GAP) / 2;

		graphics.text(this.font, "FEATURED EXPERIENCES", x, CONTENT_Y, RobloxTheme.TEXT_DIM);
		if (ClientRobloxState.experienceActive() && ClientRobloxState.experienceType() != null) {
			String status = "In Experience: " + ClientRobloxState.experienceType().displayName()
					+ "   " + ClientRobloxState.objective();
			graphics.text(this.font, status, x + width - this.font.width(status), CONTENT_Y,
					RobloxTheme.ACCENT_LIGHT);
		}

		ExperienceType[] types = ExperienceType.values();
		for (int i = 0; i < types.length; i++) {
			ExperienceType type = types[i];
			int cx = x + (i % 2) * (columnWidth + CARD_GAP);
			int cy = CONTENT_Y + 22 + (i / 2) * (CARD_HEIGHT + CARD_GAP);
			boolean playing = ClientRobloxState.experienceActive() && ClientRobloxState.experienceType() == type;
			RobloxUi.card(graphics, cx, cy, columnWidth, CARD_HEIGHT, type.accent(), playing);

			graphics.text(this.font, type.displayName(), cx + 12, cy + 9, RobloxTheme.TEXT);
			graphics.text(this.font, type.tag().toUpperCase(), cx + 12 + this.font.width(type.displayName()) + 8,
					cy + 9, type.accent());
			graphics.text(this.font, type.description(), cx + 12, cy + 22, RobloxTheme.TEXT_DIM);
			graphics.text(this.font, "Reward " + Robux.format(type.reward()), cx + 12, cy + 36, RobloxTheme.GOLD);
		}
	}
}

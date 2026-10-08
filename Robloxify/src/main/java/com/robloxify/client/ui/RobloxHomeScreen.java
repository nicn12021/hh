package com.robloxify.client.ui;

import com.robloxify.badge.Badges;
import com.robloxify.client.ClientRobloxState;
import com.robloxify.client.RobloxifyClientNetworking;
import com.robloxify.experience.ExperienceType;
import com.robloxify.util.Robux;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** The Robloxify launcher home. */
public class RobloxHomeScreen extends RobloxScreen {
	public RobloxHomeScreen() {
		super(Component.literal("Home"));
	}

	@Override
	protected RobloxTab currentTab() {
		return RobloxTab.HOME;
	}

	@Override
	protected void init() {
		super.init();
		int x = CONTENT_X;
		int width = contentWidth();
		int y = CONTENT_Y + 150;

		addRenderableWidget(new RobloxButton(x, y, width / 2 - 4, 22, Component.literal("Play Obby"),
				() -> {
					RobloxifyClientNetworking.send("experience_start", ExperienceType.OBBY.id());
					this.minecraft.gui.setScreen(null);
				}).icon("\u25B6").accent(RobloxTheme.ACCENT).primary());

		addRenderableWidget(new RobloxButton(x + width / 2 + 4, y, width / 2 - 4, 22,
				Component.literal("All Experiences"), () -> open(RobloxTab.EXPERIENCES))
				.icon("\u2630").accent(RobloxTheme.BLUE));

		y += 28;
		addRenderableWidget(new RobloxButton(x, y, width / 2 - 4, 22, Component.literal("Customise Avatar"),
				() -> open(RobloxTab.AVATAR)).icon("\u25C6").accent(RobloxTheme.PURPLE));

		addRenderableWidget(new RobloxButton(x + width / 2 + 4, y, width / 2 - 4, 22,
				Component.literal("Avatar Shop"), () -> open(RobloxTab.SHOP))
				.icon("\u25C8").accent(RobloxTheme.GOLD));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int x = CONTENT_X;
		int width = contentWidth();

		RobloxUi.panel(graphics, x, CONTENT_Y, width, 142, RobloxTheme.PANEL);
		graphics.fill(x, CONTENT_Y, x + width, CONTENT_Y + 3, RobloxTheme.ACCENT);
		graphics.text(this.font, "WELCOME BACK", x + 14, CONTENT_Y + 14, RobloxTheme.TEXT_DIM);
		String name = this.minecraft.player != null ? this.minecraft.player.getName().getString() : "Player";
		graphics.text(this.font, name, x + 14, CONTENT_Y + 28, RobloxTheme.TEXT);

		int statY = CONTENT_Y + 56;
		int column = width / 3;
		stat(graphics, x + 14, statY, "Balance", Robux.format(ClientRobloxState.robux()), RobloxTheme.GOLD);
		stat(graphics, x + 14 + column, statY, "Badges",
				ClientRobloxState.badges().size() + " / " + Badges.ALL.size(), RobloxTheme.ACCENT_LIGHT);
		stat(graphics, x + 14 + column * 2, statY, "Experiences",
				Integer.toString(ClientRobloxState.experiencesPlayed()), RobloxTheme.BLUE);

		RobloxUi.divider(graphics, x + 14, CONTENT_Y + 96, width - 28);
		graphics.text(this.font, "The loop: play an Experience, earn Robux, collect badges, customise your avatar.",
				x + 14, CONTENT_Y + 104, RobloxTheme.TEXT_DIM);
		graphics.text(this.font, "Robux is fictional in-game currency. Nothing here costs real money.",
				x + 14, CONTENT_Y + 118, RobloxTheme.TEXT_MUTED);
	}

	private void stat(GuiGraphicsExtractor graphics, int x, int y, String label, String value, int color) {
		graphics.text(this.font, label, x, y, RobloxTheme.TEXT_MUTED);
		graphics.text(this.font, value, x, y + 14, color);
	}
}

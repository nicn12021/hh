package com.robloxify.client.ui;

import com.robloxify.badge.Badge;
import com.robloxify.badge.Badges;
import com.robloxify.client.ClientRobloxState;
import com.robloxify.util.Robux;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** The badge collection. */
public class RobloxBadgesScreen extends RobloxScreen {
	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
			.withZone(ZoneId.systemDefault());
	private static final int CELL_HEIGHT = 34;
	private static final int CELL_GAP = 6;

	public RobloxBadgesScreen() {
		super(Component.literal("Badges"));
	}

	@Override
	protected RobloxTab currentTab() {
		return RobloxTab.BADGES;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int x = CONTENT_X;
		int width = contentWidth();
		int columnWidth = (width - CELL_GAP) / 2;
		int unlocked = 0;

		for (int i = 0; i < Badges.ALL.size(); i++) {
			Badge badge = Badges.ALL.get(i);
			Long time = ClientRobloxState.badges().get(badge.id());
			boolean owned = time != null;
			if (owned) {
				unlocked++;
			}
			int cx = x + (i % 2) * (columnWidth + CELL_GAP);
			int cy = CONTENT_Y + 18 + (i / 2) * (CELL_HEIGHT + CELL_GAP);
			boolean secret = badge.secret() && !owned;

			RobloxUi.card(graphics, cx, cy, columnWidth, CELL_HEIGHT,
					owned ? RobloxTheme.GOLD : RobloxTheme.BORDER, false);
			RobloxUi.badgeIcon(graphics, cx + 8, cy + 9, 16,
					owned ? RobloxTheme.GOLD : 0xFF33333D);
			graphics.text(this.font, owned ? badge.icon() : "?", cx + 14, cy + 13,
					owned ? 0xFF101018 : RobloxTheme.TEXT_MUTED);

			if (secret) {
				graphics.text(this.font, "Hidden Badge", cx + 30, cy + 7, RobloxTheme.TEXT_MUTED);
				graphics.text(this.font, "Keep exploring...", cx + 30, cy + 18, RobloxTheme.TEXT_MUTED);
			} else {
				graphics.text(this.font, badge.name(), cx + 30, cy + 7, owned ? RobloxTheme.TEXT : RobloxTheme.TEXT_DIM);
				if (owned) {
					graphics.text(this.font, FORMAT.format(Instant.ofEpochMilli(time)), cx + 30, cy + 18,
							RobloxTheme.ACCENT_LIGHT);
				} else {
					graphics.text(this.font, badge.description(), cx + 30, cy + 18, RobloxTheme.TEXT_MUTED);
				}
				String reward = "+" + Robux.format(badge.reward());
				graphics.text(this.font, reward, cx + columnWidth - 8 - this.font.width(reward), cy + 7,
						RobloxTheme.GOLD);
			}
		}
		graphics.text(this.font, unlocked + " / " + Badges.ALL.size() + " unlocked", x, CONTENT_Y,
				RobloxTheme.TEXT_DIM);
	}
}

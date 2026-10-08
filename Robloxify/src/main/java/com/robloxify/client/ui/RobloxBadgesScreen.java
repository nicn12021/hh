package com.robloxify.client.ui;

import com.robloxify.badge.Badge;
import com.robloxify.badge.Badges;
import com.robloxify.client.ClientRobloxState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Badges tab. */
public class RobloxBadgesScreen extends RobloxScreen {
	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
			.withZone(ZoneId.systemDefault());

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
		int y = 56;
		int unlocked = 0;
		for (Badge badge : Badges.ALL) {
			Long time = ClientRobloxState.badges().get(badge.id());
			boolean owned = time != null;
			if (owned) {
				unlocked++;
			}
			if (badge.secret() && !owned) {
				graphics.text(this.font, "\u25A0 ???", 16, y, RobloxTheme.DISABLED);
				graphics.text(this.font, "Hidden badge", 100, y, RobloxTheme.DISABLED);
				y += 26;
				continue;
			}
			graphics.text(this.font, (owned ? "\u2714 " : "\u25A1 ") + badge.icon() + " " + badge.name(), 16, y,
					owned ? RobloxTheme.GOLD : RobloxTheme.TEXT_DIM);
			graphics.text(this.font, badge.description(), 16, y + 10, RobloxTheme.TEXT_DIM);
			if (owned) {
				String stamp = FORMAT.format(Instant.ofEpochMilli(time));
				graphics.text(this.font, stamp + "   +" + badge.reward() + " \u25C8", this.width - 200, y + 10, RobloxTheme.ACCENT);
			}
			y += 26;
		}
		graphics.text(this.font, unlocked + " / " + Badges.ALL.size() + " unlocked", 16, this.height - 20, RobloxTheme.TEXT);
	}
}

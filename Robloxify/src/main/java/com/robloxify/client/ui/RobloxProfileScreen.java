package com.robloxify.client.ui;

import com.robloxify.badge.Badges;
import com.robloxify.client.ClientRobloxState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Profile tab: player statistics. */
public class RobloxProfileScreen extends RobloxScreen {
	public RobloxProfileScreen() {
		super(Component.literal("Profile"));
	}

	@Override
	protected RobloxTab currentTab() {
		return RobloxTab.PROFILE;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		String name = this.minecraft.player != null ? this.minecraft.player.getName().getString() : "Player";
		graphics.text(this.font, name, 16, 56, RobloxTheme.TEXT);
		graphics.fill(16, 68, 220, 69, RobloxTheme.ACCENT);

		line(graphics, 82, "Robux", "\u25C8 " + ClientRobloxState.formatNumber(ClientRobloxState.robux()));
		line(graphics, 98, "Badges", ClientRobloxState.badges().size() + " / " + Badges.ALL.size());
		line(graphics, 114, "Blocks mined", ClientRobloxState.formatNumber(ClientRobloxState.blocksMined()));
		line(graphics, 130, "Blocks placed", ClientRobloxState.formatNumber(ClientRobloxState.blocksPlaced()));
		line(graphics, 146, "Obbies completed", String.valueOf(ClientRobloxState.obbiesCompleted()));
		line(graphics, 162, "Best obby time", ClientRobloxState.obbyBestMillis() > 0
				? String.format(java.util.Locale.US, "%.2fs", ClientRobloxState.obbyBestMillis() / 1000.0) : "-");
		line(graphics, 178, "Avatar", ClientRobloxState.selfAvatar() ? "Roblox" : "Minecraft");
	}

	private void line(GuiGraphicsExtractor graphics, int y, String label, String value) {
		graphics.text(this.font, label, 16, y, RobloxTheme.TEXT_DIM);
		graphics.text(this.font, value, 140, y, RobloxTheme.TEXT);
	}
}

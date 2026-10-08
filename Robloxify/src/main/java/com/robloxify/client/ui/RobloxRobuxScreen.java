package com.robloxify.client.ui;

import com.robloxify.client.ClientRobloxState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Robux tab. */
public class RobloxRobuxScreen extends RobloxScreen {
	public RobloxRobuxScreen() {
		super(Component.literal("Robux"));
	}

	@Override
	protected RobloxTab currentTab() {
		return RobloxTab.ROBUX;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int centerX = this.width / 2;
		graphics.centeredText(this.font, "\u25C8 " + ClientRobloxState.formatNumber(ClientRobloxState.robux()),
				centerX, 60, RobloxTheme.GOLD);
		graphics.centeredText(this.font, "Robux is a purely fictional in-game currency.", centerX, 84, RobloxTheme.TEXT_DIM);
		graphics.centeredText(this.font, "No real money, no purchases, no connection to Roblox.", centerX, 96, RobloxTheme.TEXT_DIM);
		graphics.centeredText(this.font, "Earn it by unlocking badges, finishing obbies and placing studs.", centerX, 116, RobloxTheme.TEXT);
		graphics.centeredText(this.font, "Test commands: /robloxify robux add <amount> | set <amount>", centerX, 134, RobloxTheme.TEXT_DIM);
	}
}

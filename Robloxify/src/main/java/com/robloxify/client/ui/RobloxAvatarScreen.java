package com.robloxify.client.ui;

import com.robloxify.client.ClientRobloxState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** The avatar customisation editor. */
public class RobloxAvatarScreen extends RobloxCatalogScreen {
	public RobloxAvatarScreen() {
		super(Component.literal("Avatar"));
	}

	@Override
	protected RobloxTab currentTab() {
		return RobloxTab.AVATAR;
	}

	@Override
	protected Mode mode() {
		return Mode.EQUIP;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		String status = ClientRobloxState.selfAvatar() ? "Avatar: ROBLOX" : "Avatar: MINECRAFT";
		graphics.text(this.font, status, this.width - 10 - this.font.width(status), CONTENT_Y + 4,
				ClientRobloxState.selfAvatar() ? RobloxTheme.ACCENT_LIGHT : RobloxTheme.TEXT_DIM);
	}
}

package com.robloxify.client.ui;

import com.robloxify.client.ClientRobloxState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Everything the player already owns. */
public class RobloxInventoryScreen extends RobloxCatalogScreen {
	public RobloxInventoryScreen() {
		super(Component.literal("Inventory"));
	}

	@Override
	protected RobloxTab currentTab() {
		return RobloxTab.INVENTORY;
	}

	@Override
	protected Mode mode() {
		return Mode.OWNED;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		String count = ClientRobloxState.owned().size() + " items owned";
		graphics.text(this.font, count, this.width - 10 - this.font.width(count), CONTENT_Y + 4,
				RobloxTheme.TEXT_DIM);
	}
}

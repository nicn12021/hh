package com.robloxify.client.ui;

import com.robloxify.client.ClientRobloxState;
import com.robloxify.client.RobloxifyClientNetworking;
import com.robloxify.shop.Shop;
import com.robloxify.shop.ShopItem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Shop tab: spend Robux on cosmetics. */
public class RobloxShopScreen extends RobloxScreen {
	public RobloxShopScreen() {
		super(Component.literal("Shop"));
	}

	@Override
	protected RobloxTab currentTab() {
		return RobloxTab.SHOP;
	}

	@Override
	protected void init() {
		super.init();
		int y = 56;
		for (ShopItem item : Shop.ALL) {
			addRenderableWidget(Button.builder(buttonLabel(item), button -> {
				if (!ClientRobloxState.owns(item.id())) {
					RobloxifyClientNetworking.send("buy", item.id());
				}
				button.setMessage(buttonLabel(item));
			}).bounds(this.width - 110, y, 100, 20).build());
			y += 28;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int y = 56;
		for (ShopItem item : Shop.ALL) {
			graphics.text(this.font, item.name(), 16, y + 2, RobloxTheme.TEXT);
			graphics.text(this.font, item.description(), 16, y + 13, RobloxTheme.TEXT_DIM);
			graphics.text(this.font, "\u25C8 " + item.price(), this.width - 150, y + 7, RobloxTheme.GOLD);
			y += 28;
		}
	}

	private static Component buttonLabel(ShopItem item) {
		return Component.literal(ClientRobloxState.owns(item.id()) ? "Owned" : "Buy");
	}
}

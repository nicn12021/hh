package com.robloxify.client.ui;

import net.minecraft.network.chat.Component;

/** The avatar shop. */
public class RobloxShopScreen extends RobloxCatalogScreen {
	public RobloxShopScreen() {
		super(Component.literal("Avatar Shop"));
	}

	@Override
	protected RobloxTab currentTab() {
		return RobloxTab.SHOP;
	}

	@Override
	protected Mode mode() {
		return Mode.BUY;
	}
}

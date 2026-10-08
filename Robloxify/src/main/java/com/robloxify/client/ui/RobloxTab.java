package com.robloxify.client.ui;

import net.minecraft.client.gui.screens.Screen;

/** The sections of the Robloxify interface, in sidebar order. */
public enum RobloxTab {
	HOME("Home", "\u25A0"),
	EXPERIENCES("Play", "\u25B6"),
	AVATAR("Avatar", "\u25C6"),
	SHOP("Shop", "\u25C8"),
	BADGES("Badges", "\u2605"),
	INVENTORY("Items", "\u25A3"),
	SETTINGS("Config", "\u2699");

	private final String label;
	private final String icon;

	RobloxTab(String label, String icon) {
		this.label = label;
		this.icon = icon;
	}

	public String label() {
		return label;
	}

	public String icon() {
		return icon;
	}

	public Screen create() {
		return switch (this) {
			case HOME -> new RobloxHomeScreen();
			case EXPERIENCES -> new RobloxExperiencesScreen();
			case AVATAR -> new RobloxAvatarScreen();
			case SHOP -> new RobloxShopScreen();
			case BADGES -> new RobloxBadgesScreen();
			case INVENTORY -> new RobloxInventoryScreen();
			case SETTINGS -> new RobloxSettingsScreen();
		};
	}
}

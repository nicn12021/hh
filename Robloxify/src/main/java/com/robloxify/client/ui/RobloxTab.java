package com.robloxify.client.ui;

import net.minecraft.client.gui.screens.Screen;

/** The tabs of the Robloxify UI. */
public enum RobloxTab {
	AVATAR("Avatar"),
	ROBUX("Robux"),
	SHOP("Shop"),
	BADGES("Badges"),
	PROFILE("Profile"),
	SETTINGS("Settings");

	private final String label;

	RobloxTab(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public Screen create() {
		return switch (this) {
			case AVATAR -> new RobloxMainScreen();
			case ROBUX -> new RobloxRobuxScreen();
			case SHOP -> new RobloxShopScreen();
			case BADGES -> new RobloxBadgesScreen();
			case PROFILE -> new RobloxProfileScreen();
			case SETTINGS -> new RobloxSettingsScreen();
		};
	}
}

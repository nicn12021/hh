package com.robloxify.util;

import java.util.Locale;

/** Robux presentation helpers. */
public final class Robux {
	/** Robloxify's own currency mark. */
	public static final String SYMBOL = "R$";

	private Robux() {
	}

	public static String format(int amount) {
		return SYMBOL + " " + String.format(Locale.US, "%,d", amount);
	}

	public static String plain(int amount) {
		return String.format(Locale.US, "%,d", amount);
	}

	/** Compact form for the HUD: 1250 -> 1.2K */
	public static String compact(int amount) {
		if (amount < 1000) {
			return Integer.toString(amount);
		}
		if (amount < 1_000_000) {
			return String.format(Locale.US, "%.1fK", amount / 1000.0);
		}
		return String.format(Locale.US, "%.1fM", amount / 1_000_000.0);
	}
}

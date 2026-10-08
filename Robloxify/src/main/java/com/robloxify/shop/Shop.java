package com.robloxify.shop;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Purely cosmetic things you can buy with Robux. No real money is ever involved. */
public final class Shop {
	public static final ShopItem SPARKLE_TRAIL = new ShopItem("sparkle_trail", "Sparkle Trail",
			"A trail of sparkles follows you around.", 150);
	public static final ShopItem SHADES = new ShopItem("shades", "Cool Shades",
			"A pair of blocky shades for your avatar.", 250);
	public static final ShopItem BUILDER_CAP = new ShopItem("builder_cap", "Builder Cap",
			"A trusty yellow cap for your avatar.", 300);
	public static final ShopItem OOF_PACK = new ShopItem("oof_pack", "OOF Pack",
			"A suspiciously familiar sound when you die.", 500);
	public static final ShopItem GOLD_STUDS = new ShopItem("gold_studs", "Gold Studs",
			"Placing a stud showers it in gold particles.", 750);

	public static final List<ShopItem> ALL = List.of(SPARKLE_TRAIL, SHADES, BUILDER_CAP, OOF_PACK, GOLD_STUDS);

	private Shop() {
	}

	@Nullable
	public static ShopItem byId(String id) {
		for (ShopItem item : ALL) {
			if (item.id().equals(id)) {
				return item;
			}
		}
		return null;
	}
}

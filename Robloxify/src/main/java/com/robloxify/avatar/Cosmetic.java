package com.robloxify.avatar;

/**
 * One avatar customisation item.
 *
 * @param id        stable id, also used for persistence
 * @param category  which slot it occupies
 * @param name      display name
 * @param price     Robux price (0 = free / default)
 * @param primary   main colour (ARGB) for textures, or 0 when unused
 * @param secondary trim / accent colour (ARGB), or 0 when unused
 * @param pattern   how the colour is applied to the skin texture
 * @param shape     geometry variant for hats / accessories
 */
public record Cosmetic(String id, CosmeticCategory category, String name, int price,
					   int primary, int secondary, Pattern pattern, String shape) {
	public enum Pattern {
		SOLID,
		TRIM,
		STRIPES,
		SPECKLE
	}

	public boolean isFree() {
		return price <= 0;
	}

	public static Cosmetic of(String id, CosmeticCategory category, String name, int price, int primary, int secondary) {
		return new Cosmetic(id, category, name, price, primary, secondary, Pattern.SOLID, "");
	}
}

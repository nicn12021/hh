package com.robloxify.avatar;

/** The avatar customisation categories, in the order they appear in the editor. */
public enum CosmeticCategory {
	FACE("Face"),
	SHIRT("Shirt"),
	PANTS("Pants"),
	HAT("Hat"),
	ACCESSORY("Accessory"),
	EFFECT("Effect"),
	ANIMATION("Animation");

	private final String label;

	CosmeticCategory(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public String key() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}
}

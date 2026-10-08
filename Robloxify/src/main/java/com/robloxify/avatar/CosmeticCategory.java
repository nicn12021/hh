package com.robloxify.avatar;

/** The avatar customisation categories, in the order they appear in the editor. */
public enum CosmeticCategory {
	BODY("Body"),
	FACE("Face"),
	HAIR("Hair"),
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

	/** Compact label for the category tabs, which have to fit nine across. */
	public String shortLabel() {
		return switch (this) {
			case BODY -> "Body";
			case FACE -> "Face";
			case HAIR -> "Hair";
			case SHIRT -> "Shirt";
			case PANTS -> "Pants";
			case HAT -> "Hat";
			case ACCESSORY -> "Extra";
			case EFFECT -> "FX";
			case ANIMATION -> "Anim";
		};
	}

	public String key() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}
}

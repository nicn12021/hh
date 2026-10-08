package com.robloxify.avatar;

import java.util.Objects;

/** Which cosmetic is equipped in each slot. Plain fields so Gson can serialise it. */
public final class AvatarAppearance {
	public String body = "warm";
	public String face = "neutral";
	public String hair = "brown";
	public String shirt = "classic_blue";
	public String pants = "classic_green";
	public String hat = CosmeticCatalog.NONE;
	public String accessory = CosmeticCatalog.NONE;
	public String effect = CosmeticCatalog.NONE;
	public String animation = "classic";

	public String slot(CosmeticCategory category) {
		return switch (category) {
			case BODY -> body;
			case FACE -> face;
			case HAIR -> hair;
			case SHIRT -> shirt;
			case PANTS -> pants;
			case HAT -> hat;
			case ACCESSORY -> accessory;
			case EFFECT -> effect;
			case ANIMATION -> animation;
		};
	}

	public void set(CosmeticCategory category, String id) {
		switch (category) {
			case BODY -> body = id;
			case FACE -> face = id;
			case HAIR -> hair = id;
			case SHIRT -> shirt = id;
			case PANTS -> pants = id;
			case HAT -> hat = id;
			case ACCESSORY -> accessory = id;
			case EFFECT -> effect = id;
			case ANIMATION -> animation = id;
		}
	}

	public Cosmetic cosmetic(CosmeticCategory category) {
		Cosmetic cosmetic = CosmeticCatalog.byId(slot(category));
		return cosmetic != null ? cosmetic : CosmeticCatalog.defaultOf(category);
	}

	/** Stable cache key for the generated skin texture. */
	public String key() {
		return body + '|' + face + '|' + hair + '|' + shirt + '|' + pants + '|' + hat + '|' + accessory;
	}

	public AvatarAppearance copy() {
		AvatarAppearance copy = new AvatarAppearance();
		copy.body = body;
		copy.face = face;
		copy.hair = hair;
		copy.shirt = shirt;
		copy.pants = pants;
		copy.hat = hat;
		copy.accessory = accessory;
		copy.effect = effect;
		copy.animation = animation;
		return copy;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AvatarAppearance that)) {
			return false;
		}
		return body.equals(that.body) && face.equals(that.face) && hair.equals(that.hair) && shirt.equals(that.shirt) && pants.equals(that.pants)
				&& hat.equals(that.hat) && accessory.equals(that.accessory)
				&& effect.equals(that.effect) && animation.equals(that.animation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(body, face, hair, shirt, pants, hat, accessory, effect, animation);
	}
}

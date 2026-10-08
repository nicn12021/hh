package com.robloxify.client.avatar;

import com.mojang.blaze3d.platform.NativeImage;
import com.robloxify.Robloxify;
import com.robloxify.avatar.AvatarAppearance;
import com.robloxify.avatar.Cosmetic;
import com.robloxify.avatar.CosmeticCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds the avatar skin texture at runtime from the equipped cosmetics.
 * <p>
 * The texture is 128x128. The classic 64x64 player layout is used for the body, and the extra
 * space on the right holds the geometry for hats, accessories and shades. Textures are cached per
 * appearance and evicted oldest-first, so a server full of players cannot grow this without bound.
 */
public final class AvatarSkinFactory {
	private static final int SIZE = 128;
	private static final int MAX_CACHE = 48;

	private static final Map<String, Identifier> CACHE = new LinkedHashMap<>();
	private static int counter;

	// Base palette - all original colours.
	private static final int SKIN = 0xFFE8B96B;
	private static final int SKIN_DARK = 0xFFD9A455;
	private static final int HAIR = 0xFF4A3728;
	private static final int MOUTH = 0xFF8C3B2E;
	private static final int TONGUE = 0xFFE05A5A;

	private AvatarSkinFactory() {
	}

	public static Identifier textureFor(AvatarAppearance appearance) {
		String key = appearance.key();
		Identifier cached = CACHE.get(key);
		if (cached != null) {
			return cached;
		}

		NativeImage image = compose(appearance);
		Identifier id = Robloxify.id("generated/avatar_" + (counter++));
		Minecraft.getInstance().getTextureManager().register(id,
				new DynamicTexture(() -> "Robloxify avatar", image));

		CACHE.put(key, id);
		if (CACHE.size() > MAX_CACHE) {
			Iterator<Map.Entry<String, Identifier>> iterator = CACHE.entrySet().iterator();
			if (iterator.hasNext()) {
				Identifier oldest = iterator.next().getValue();
				iterator.remove();
				Minecraft.getInstance().getTextureManager().release(oldest);
			}
		}
		return id;
	}

	// ------------------------------------------------------------- composition

	private static NativeImage compose(AvatarAppearance appearance) {
		NativeImage image = new NativeImage(SIZE, SIZE, false);
		image.fillRect(0, 0, SIZE, SIZE, SKIN);

		Cosmetic shirt = appearance.cosmetic(CosmeticCategory.SHIRT);
		Cosmetic pants = appearance.cosmetic(CosmeticCategory.PANTS);
		Cosmetic hat = appearance.cosmetic(CosmeticCategory.HAT);
		Cosmetic accessory = appearance.cosmetic(CosmeticCategory.ACCESSORY);

		// --- head (0,0)-(32,16) -------------------------------------------------
		image.fillRect(0, 0, 32, 16, SKIN);
		// hair on the top face of the head
		image.fillRect(8, 0, 16, 8, HAIR);
		image.fillRect(0, 8, 8, 8, HAIR);
		image.fillRect(24, 8, 8, 8, HAIR);

		// --- hat overlay layer (32,0)-(64,16): kept skin coloured and hidden ---
		image.fillRect(32, 0, 32, 16, SKIN);

		// --- body / shirt (16,16)-(40,32) --------------------------------------
		paint(image, 16, 16, 24, 16, shirt, true);

		// --- arms (40,16)-(64,32): sleeves on top, skin forearms --------------
		paint(image, 40, 16, 24, 16, shirt, true);
		image.fillRect(0, 32, 24, 8, SKIN_DARK);

		// --- legs (0,16)-(16,32) ----------------------------------------------
		paint(image, 0, 16, 16, 16, pants, false);
		image.fillRect(24, 32, 24, 8, 0);
		paint(image, 24, 32, 24, 8, pants, false);

		// --- face -------------------------------------------------------------
		drawFace(image, appearance.cosmetic(CosmeticCategory.FACE));

		// --- hat geometry -----------------------------------------------------
		paintShape(image, hat, 64, 0);
		// --- accessory geometry ----------------------------------------------
		paintShape(image, accessory, 64, 64);

		// --- shades -----------------------------------------------------------
		image.fillRect(96, 64, 18, 3, 0xFF23232B);
		return image;
	}

	/** Applies a shirt or pants colour, with the cosmetic's pattern. */
	private static void paint(NativeImage image, int x, int y, int width, int height, Cosmetic cosmetic, boolean trimTop) {
		int primary = cosmetic.primary() == 0 ? 0xFFB0B0B0 : cosmetic.primary();
		int secondary = cosmetic.secondary() == 0 ? primary : cosmetic.secondary();
		image.fillRect(x, y, width, height, primary);

		switch (cosmetic.pattern()) {
			case TRIM -> {
				image.fillRect(x, y, width, 2, secondary);
				image.fillRect(x, y + height - 2, width, 2, secondary);
			}
			case STRIPES -> {
				for (int stripe = 0; stripe < height; stripe += 3) {
					int color = ((stripe / 3) % 2 == 0) ? primary : secondary;
					image.fillRect(x, y + stripe, width, 1, color);
				}
			}
			case SPECKLE -> {
				for (int i = 0; i < width; i += 4) {
					for (int j = 0; j < height; j += 4) {
						image.setPixel(x + i, y + j, secondary);
					}
				}
			}
			default -> {
				if (trimTop) {
					image.fillRect(x, y, width, 1, secondary);
				}
			}
		}
	}

	/** Paints the geometry regions used by hats and accessories. */
	private static void paintShape(NativeImage image, Cosmetic cosmetic, int baseX, int baseY) {
		int primary = cosmetic.primary() == 0 ? 0xFFB0B0B0 : cosmetic.primary();
		int secondary = cosmetic.secondary() == 0 ? primary : cosmetic.secondary();
		switch (cosmetic.shape()) {
			case "cap" -> {
				image.fillRect(64, 0, 36, 11, primary);
				image.fillRect(64, 0, 36, 2, secondary);
			}
			case "tophat" -> {
				image.fillRect(64, 16, 28, 12, primary);
				image.fillRect(64, 16, 28, 2, secondary);
				image.fillRect(64, 32, 36, 10, secondary);
			}
			case "crown" -> {
				image.fillRect(64, 48, 36, 12, primary);
				for (int i = 0; i < 36; i += 6) {
					image.fillRect(64 + i, 48, 2, 4, secondary);
				}
			}
			case "headphones" -> {
				image.fillRect(64, 64, 22, 3, secondary);
				image.fillRect(64, 72, 8, 6, primary);
			}
			case "backpack" -> image.fillRect(64, 80, 16, 9, primary);
			case "wings" -> image.fillRect(64, 96, 12, 13, primary);
			case "shoulder_pads" -> image.fillRect(64, 112, 16, 5, primary);
			default -> {
			}
		}
	}

	// ------------------------------------------------------------------- faces

	private static void drawFace(NativeImage image, Cosmetic face) {
		int ink = face.primary() == 0 ? 0xFF33333F : face.primary();
		switch (face.shape()) {
			case "happy" -> {
				eye(image, ink, 2, 3);
				eye(image, ink, 5, 3);
				pixel(image, ink, 2, 5);
				pixel(image, ink, 3, 6);
				pixel(image, ink, 4, 6);
				pixel(image, ink, 5, 6);
				pixel(image, ink, 6, 5);
			}
			case "surprised" -> {
				bigEye(image, ink, 1, 2);
				bigEye(image, ink, 5, 2);
				pixel(image, MOUTH, 3, 5);
				pixel(image, MOUTH, 4, 6);
				pixel(image, MOUTH, 3, 7);
			}
			case "silly" -> {
				eye(image, ink, 2, 3);
				pixel(image, ink, 5, 3);
				pixel(image, ink, 6, 4);
				pixel(image, ink, 5, 5);
				pixel(image, ink, 3, 6);
				pixel(image, MOUTH, 4, 6);
				pixel(image, TONGUE, 4, 7);
			}
			case "angry" -> {
				eye(image, ink, 2, 4);
				eye(image, ink, 5, 4);
				pixel(image, ink, 2, 2);
				pixel(image, ink, 3, 3);
				pixel(image, ink, 5, 3);
				pixel(image, ink, 6, 2);
				pixel(image, ink, 3, 6);
				pixel(image, ink, 4, 6);
				pixel(image, ink, 5, 6);
			}
			case "sleepy" -> {
				pixel(image, ink, 2, 4);
				pixel(image, ink, 3, 4);
				pixel(image, ink, 5, 4);
				pixel(image, ink, 6, 4);
				pixel(image, MOUTH, 4, 6);
			}
			default -> {
				eye(image, ink, 2, 3);
				eye(image, ink, 5, 3);
				pixel(image, ink, 3, 6);
				pixel(image, ink, 4, 6);
				pixel(image, ink, 5, 6);
			}
		}
	}

	/** Face coordinates are relative to the front of the head, which is (8,8) in the texture. */
	private static void pixel(NativeImage image, int color, int x, int y) {
		image.setPixel(8 + x, 8 + y, color);
	}

	private static void eye(NativeImage image, int color, int x, int y) {
		pixel(image, color, x, y);
		pixel(image, color, x + 1, y);
	}

	private static void bigEye(NativeImage image, int color, int x, int y) {
		for (int dx = 0; dx < 2; dx++) {
			for (int dy = 0; dy < 2; dy++) {
				pixel(image, color, x + dx, y + dy);
			}
		}
	}
}

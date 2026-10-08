package com.robloxify.client;

import com.robloxify.config.RobloxifyConfig;
import com.robloxify.net.RobloxifyPayloads;
import com.robloxify.util.Emotes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Mannequin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Everything the client needs to know, kept in one cheap cache. */
public final class ClientRobloxState {
	private static int robux;
	private static int blocksMined;
	private static int blocksPlaced;
	private static boolean selfAvatar;
	private static int obbiesCompleted;
	private static long obbyBestMillis;
	private static final Map<String, Long> BADGES = new LinkedHashMap<>();
	private static final List<String> OWNED = new ArrayList<>();
	private static final Set<UUID> ROBLOX_PLAYERS = new HashSet<>();
	private static final Map<UUID, String> EMOTES = new HashMap<>();
	private static final Map<UUID, Integer> EMOTE_TICKS = new HashMap<>();

	private static boolean obbyActive;
	private static int obbyCheckpoint;
	private static int obbyTotal;
	private static long obbyElapsedMillis;
	private static long obbySyncedAt;

	private static String bannerTitle = "";
	private static String bannerSubtitle = "";
	private static int bannerTicks;
	private static int bannerColor = 0xFFFFC64A;

	private ClientRobloxState() {
	}

	// ---- Incoming --------------------------------------------------------

	public static void applyProfile(RobloxifyPayloads.Profile payload) {
		robux = payload.robux();
		blocksMined = payload.blocksMined();
		blocksPlaced = payload.blocksPlaced();
		selfAvatar = payload.robloxAvatar();
		obbiesCompleted = payload.obbiesCompleted();
		obbyBestMillis = payload.obbyBestMillis();
		BADGES.clear();
		BADGES.putAll(payload.badges());
		OWNED.clear();
		OWNED.addAll(payload.owned());
	}

	public static void setAvatar(UUID player, boolean roblox) {
		if (roblox) {
			ROBLOX_PLAYERS.add(player);
		} else {
			ROBLOX_PLAYERS.remove(player);
		}
	}

	public static void playEmote(UUID player, String emote) {
		EMOTES.put(player, emote);
		EMOTE_TICKS.put(player, Emotes.duration(emote));
	}

	public static void onBadgeUnlocked(String badgeId, int reward) {
		var badge = com.robloxify.badge.Badges.byId(badgeId);
		String name = badge != null ? badge.icon() + " " + badge.name() : badgeId;
		String description = badge != null ? badge.description() : "";
		showBanner("Badge unlocked: " + name, description + "   +" + reward + " Robux", 0xFFFFC64A);
		ClientSounds.play(com.robloxify.sound.RobloxifySounds.BADGE_UNLOCK, 1.0F);
	}

	public static void applyObby(RobloxifyPayloads.Obby payload) {
		obbyActive = payload.active();
		obbyCheckpoint = payload.checkpoint();
		obbyTotal = payload.total();
		obbyElapsedMillis = payload.elapsedMillis();
		obbySyncedAt = System.currentTimeMillis();
		if (payload.completed()) {
			showBanner("Experience complete!", "Obby finished in " + (payload.elapsedMillis() / 1000.0) + "s", 0xFF7CFC00);
		}
	}

	public static void showBanner(String title, String subtitle, int color) {
		bannerTitle = title;
		bannerSubtitle = subtitle;
		bannerColor = color;
		bannerTicks = 100;
	}

	/** Called once per client tick. */
	public static void tick() {
		if (!EMOTE_TICKS.isEmpty()) {
			EMOTE_TICKS.entrySet().removeIf(entry -> entry.getValue() - 1 <= 0);
			for (Map.Entry<UUID, Integer> entry : EMOTE_TICKS.entrySet()) {
				entry.setValue(entry.getValue() - 1);
			}
			EMOTES.keySet().removeIf(uuid -> !EMOTE_TICKS.containsKey(uuid));
		}
		if (bannerTicks > 0) {
			bannerTicks--;
		}
	}

	// ---- Queries ---------------------------------------------------------

	public static boolean isRobloxPlayer(UUID uuid) {
		return ROBLOX_PLAYERS.contains(uuid);
	}

	public static String emoteOf(UUID uuid) {
		return EMOTES.get(uuid);
	}

	public static float emoteProgress(UUID uuid) {
		Integer left = EMOTE_TICKS.get(uuid);
		if (left == null) {
			return 0.0F;
		}
		String emote = EMOTES.get(uuid);
		int total = Math.max(1, Emotes.duration(emote));
		return 1.0F - (left / (float) total);
	}


	/** Looks up an entity by its network id in the current client level. */
	public static Entity entityFor(int entityId) {
		Minecraft client = Minecraft.getInstance();
		return client.level == null ? null : client.level.getEntity(entityId);
	}

	/** True when this entity should be drawn with the blocky Roblox avatar. */
	public static boolean isRobloxEntity(int entityId) {
		Entity entity = entityFor(entityId);
		if (entity == null) {
			return false;
		}
		if (entity instanceof Mannequin) {
			return RobloxifyConfig.get().easterEggs;
		}
		return ROBLOX_PLAYERS.contains(entity.getUUID());
	}

	public static int robux() {
		return robux;
	}

	public static int blocksMined() {
		return blocksMined;
	}

	public static int blocksPlaced() {
		return blocksPlaced;
	}

	public static boolean selfAvatar() {
		return selfAvatar;
	}

	public static int obbiesCompleted() {
		return obbiesCompleted;
	}

	public static long obbyBestMillis() {
		return obbyBestMillis;
	}

	public static Map<String, Long> badges() {
		return BADGES;
	}

	public static List<String> owned() {
		return OWNED;
	}

	public static boolean owns(String id) {
		return OWNED.contains(id);
	}

	public static boolean obbyActive() {
		return obbyActive;
	}

	public static int obbyCheckpoint() {
		return obbyCheckpoint;
	}

	public static int obbyTotal() {
		return obbyTotal;
	}

	public static long obbyElapsedMillis() {
		return obbyActive ? obbyElapsedMillis + (System.currentTimeMillis() - obbySyncedAt) : obbyElapsedMillis;
	}

	public static boolean hasBanner() {
		return bannerTicks > 0;
	}

	public static String bannerTitle() {
		return bannerTitle;
	}

	public static String bannerSubtitle() {
		return bannerSubtitle;
	}

	public static int bannerColor() {
		return bannerColor;
	}

	public static String formatNumber(long value) {
		return String.format(Locale.US, "%,d", value);
	}
}

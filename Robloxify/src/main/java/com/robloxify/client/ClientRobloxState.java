package com.robloxify.client;

import com.robloxify.avatar.AvatarAppearance;
import com.robloxify.config.RobloxifyConfig;
import com.robloxify.experience.ExperienceType;
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
	private static int experiencesPlayed;
	private static long obbyBestMillis;
	private static final Map<String, Long> BADGES = new LinkedHashMap<>();
	private static final List<String> OWNED = new ArrayList<>();
	private static final AvatarAppearance SELF_APPEARANCE = new AvatarAppearance();

	private static final Set<UUID> ROBLOX_PLAYERS = new HashSet<>();
	private static final Map<UUID, AvatarAppearance> APPEARANCES = new HashMap<>();
	private static final Map<UUID, String> EMOTES = new HashMap<>();
	private static final Map<UUID, Integer> EMOTE_TICKS = new HashMap<>();

	private static boolean experienceActive;
	private static ExperienceType experienceType;
	private static int checkpoint;
	private static int checkpointTotal;
	private static long elapsedMillis;
	private static long syncedAt;
	private static String objective = "";

	private ClientRobloxState() {
	}

	// ---- Incoming --------------------------------------------------------

	public static void applyProfile(RobloxifyPayloads.Profile payload) {
		robux = payload.robux();
		blocksMined = payload.blocksMined();
		blocksPlaced = payload.blocksPlaced();
		selfAvatar = payload.robloxAvatar();
		obbiesCompleted = payload.obbiesCompleted();
		experiencesPlayed = payload.experiencesPlayed();
		obbyBestMillis = payload.obbyBestMillis();
		BADGES.clear();
		BADGES.putAll(payload.badges());
		OWNED.clear();
		OWNED.addAll(payload.owned());
		copyInto(SELF_APPEARANCE, payload.appearance());
	}

	private static void copyInto(AvatarAppearance target, AvatarAppearance source) {
		if (source == null) {
			return;
		}
		target.face = source.face;
		target.shirt = source.shirt;
		target.pants = source.pants;
		target.hat = source.hat;
		target.accessory = source.accessory;
		target.effect = source.effect;
		target.animation = source.animation;
	}

	public static void applyAppearance(RobloxifyPayloads.Appearance payload) {
		if (payload.roblox()) {
			ROBLOX_PLAYERS.add(payload.player());
		} else {
			ROBLOX_PLAYERS.remove(payload.player());
		}
		AvatarAppearance appearance = APPEARANCES.computeIfAbsent(payload.player(), key -> new AvatarAppearance());
		copyInto(appearance, payload.appearance());
	}

	public static void playEmote(UUID player, String emote) {
		EMOTES.put(player, emote);
		EMOTE_TICKS.put(player, Emotes.duration(emote));
	}

	public static void applyExperience(RobloxifyPayloads.Experience payload) {
		experienceActive = payload.active();
		experienceType = ExperienceType.byId(payload.experienceId());
		checkpoint = payload.checkpoint();
		checkpointTotal = payload.total();
		elapsedMillis = payload.elapsedMillis();
		objective = payload.objective();
		syncedAt = System.currentTimeMillis();
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
	}

	// ---- Queries ---------------------------------------------------------

	public static Entity entityFor(int entityId) {
		Minecraft client = Minecraft.getInstance();
		return client.level == null ? null : client.level.getEntity(entityId);
	}

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

	/** The appearance to draw for an entity, or null when it is a plain Minecraft player. */
	public static AvatarAppearance appearanceFor(int entityId) {
		Entity entity = entityFor(entityId);
		if (entity == null || !isRobloxEntity(entityId)) {
			return null;
		}
		if (entity == Minecraft.getInstance().player) {
			return SELF_APPEARANCE;
		}
		return APPEARANCES.getOrDefault(entity.getUUID(), SELF_APPEARANCE);
	}

	public static String emoteOf(UUID uuid) {
		return EMOTES.get(uuid);
	}

	public static float emoteProgress(UUID uuid) {
		Integer left = EMOTE_TICKS.get(uuid);
		if (left == null) {
			return 0.0F;
		}
		int total = Math.max(1, Emotes.duration(EMOTES.get(uuid)));
		return 1.0F - (left / (float) total);
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

	public static int experiencesPlayed() {
		return experiencesPlayed;
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

	public static AvatarAppearance selfAppearance() {
		return SELF_APPEARANCE;
	}

	public static boolean experienceActive() {
		return experienceActive;
	}

	public static ExperienceType experienceType() {
		return experienceType;
	}

	public static int checkpoint() {
		return checkpoint;
	}

	public static int checkpointTotal() {
		return checkpointTotal;
	}

	public static long elapsedMillis() {
		return experienceActive ? elapsedMillis + (System.currentTimeMillis() - syncedAt) : elapsedMillis;
	}

	public static String objective() {
		return objective;
	}
}

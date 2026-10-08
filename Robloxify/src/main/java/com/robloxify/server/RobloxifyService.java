package com.robloxify.server;

import com.robloxify.avatar.AvatarAppearance;
import com.robloxify.avatar.Cosmetic;
import com.robloxify.avatar.CosmeticCatalog;
import com.robloxify.avatar.CosmeticCategory;
import com.robloxify.badge.Badge;
import com.robloxify.badge.Badges;
import com.robloxify.config.RobloxifyConfig;
import com.robloxify.data.Profile;
import com.robloxify.data.RobloxifyData;
import com.robloxify.experience.ExperienceManager;
import com.robloxify.experience.ExperienceType;
import com.robloxify.net.RobloxifyNetworking;
import com.robloxify.net.RobloxifyPayloads;
import com.robloxify.sound.RobloxifySounds;
import com.robloxify.util.Emotes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/** All authoritative Robloxify logic. Everything here runs on the logical server. */
public final class RobloxifyService {
	public static final int EXPERIENCES_FOR_EXPLORER = 3;
	public static final int ITEMS_FOR_COLLECTOR = 8;
	public static final int ROBUX_FOR_MILLIONAIRE = 10_000;

	private RobloxifyService() {
	}

	public static Profile profile(ServerPlayer player) {
		return RobloxifyData.get().profile(player.getUUID());
	}

	public static void syncProfile(ServerPlayer player) {
		Profile profile = profile(player);
		RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Profile(
				profile.robux, profile.blocksMined, profile.blocksPlaced, profile.robloxAvatar,
				profile.obbiesCompleted, profile.experiencesPlayed, profile.obbyBestMillis,
				new LinkedHashMap<>(profile.badges), new ArrayList<>(profile.owned),
				profile.appearance.copy()));
	}

	/** Pushes this player's look to everybody, including themselves. */
	public static void broadcastAppearance(ServerPlayer player) {
		Profile profile = profile(player);
		RobloxifyNetworking.broadcast(player.level().getServer(), new RobloxifyPayloads.Appearance(
				player.getUUID(), profile.robloxAvatar, profile.appearance.copy()));
	}

	public static void notify(ServerPlayer player, String kind, String title, String subtitle, int robux) {
		if (!RobloxifyConfig.get().notifications) {
			return;
		}
		RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Notification(kind, title, subtitle, robux));
	}

	public static void play(ServerPlayer player, SoundEvent sound) {
		play(player, sound, 1.0F);
	}

	public static void play(ServerPlayer player, SoundEvent sound, float pitch) {
		if (!RobloxifyConfig.get().robloxSounds) {
			return;
		}
		player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 0.7F, pitch);
	}

	// ---------------------------------------------------------------- badges

	public static void grantBadge(ServerPlayer player, Badge badge) {
		if (!RobloxifyConfig.get().badges) {
			return;
		}
		Profile profile = profile(player);
		if (!profile.grantBadge(badge.id(), System.currentTimeMillis())) {
			return;
		}
		profile.robux += badge.reward();
		RobloxifyData.get().markDirty();
		notify(player, "badge", "Badge Awarded!", badge.icon() + "  " + badge.name(), badge.reward());
		play(player, RobloxifySounds.BADGE_UNLOCK);
		player.sendSystemMessage(Component.literal("[Robloxify] ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(badge.icon() + " " + badge.name()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal("  +" + badge.reward() + " Robux").withStyle(ChatFormatting.GREEN)));
		syncProfile(player);
		checkWealthBadges(player);
	}

	public static void checkWealthBadges(ServerPlayer player) {
		Profile profile = profile(player);
		if (profile.robux >= ROBUX_FOR_MILLIONAIRE) {
			grantBadge(player, Badges.MILLIONAIRE);
		}
		if (profile.owned.size() >= ITEMS_FOR_COLLECTOR) {
			grantBadge(player, Badges.AVATAR_COLLECTOR);
		}
	}

	public static void checkExplorerBadge(ServerPlayer player) {
		if (profile(player).completedExperiences.size() >= EXPERIENCES_FOR_EXPLORER) {
			grantBadge(player, Badges.EXPLORER);
		}
	}

	// ----------------------------------------------------------------- robux

	public static void addRobux(ServerPlayer player, int amount) {
		Profile profile = profile(player);
		profile.robux = Math.max(0, profile.robux + amount);
		RobloxifyData.get().markDirty();
		syncProfile(player);
		if (amount > 0) {
			checkWealthBadges(player);
		}
	}

	public static void setRobux(ServerPlayer player, int amount) {
		Profile profile = profile(player);
		profile.robux = Math.max(0, amount);
		RobloxifyData.get().markDirty();
		syncProfile(player);
		notify(player, "robux", "Balance updated", com.robloxify.util.Robux.format(profile.robux), 0);
	}

	// ---------------------------------------------------------------- avatar

	public static void setAvatar(ServerPlayer player, boolean roblox) {
		if (!RobloxifyConfig.get().robloxAvatar) {
			notify(player, "fail", "Avatar disabled", "The Roblox avatar is off in Settings.", 0);
			return;
		}
		Profile profile = profile(player);
		if (profile.robloxAvatar == roblox) {
			return;
		}
		profile.robloxAvatar = roblox;
		RobloxifyData.get().markDirty();
		broadcastAppearance(player);
		play(player, RobloxifySounds.AVATAR_SWITCH);
		notify(player, "avatar", roblox ? "Roblox avatar on" : "Minecraft player",
				roblox ? "Looking blocky." : "Back to vanilla.", 0);
		if (roblox) {
			grantBadge(player, Badges.ROBLOXIAN);
		}
		checkEnderRobloxian(player);
		syncProfile(player);
	}

	public static void checkEnderRobloxian(ServerPlayer player) {
		if (profile(player).robloxAvatar && player.level().dimension() == Level.END) {
			grantBadge(player, Badges.ENDER_ROBLOXIAN);
		}
	}

	/** Equips an owned cosmetic into its slot. */
	public static void equip(ServerPlayer player, String cosmeticId) {
		Cosmetic cosmetic = CosmeticCatalog.byId(cosmeticId);
		if (cosmetic == null) {
			return;
		}
		Profile profile = profile(player);
		if (!profile.owns(cosmeticId)) {
			notify(player, "fail", "Not owned", "Buy " + cosmetic.name() + " in the Shop first.", 0);
			return;
		}
		AvatarAppearance appearance = profile.appearance;
		if (cosmetic.id().equals(appearance.slot(cosmetic.category()))) {
			// Clicking the equipped item takes it off (except for body slots that must keep a value).
			if (cosmetic.category() != CosmeticCategory.FACE && cosmetic.category() != CosmeticCategory.SHIRT
					&& cosmetic.category() != CosmeticCategory.PANTS && cosmetic.category() != CosmeticCategory.ANIMATION) {
				appearance.set(cosmetic.category(), CosmeticCatalog.NONE);
			}
		} else {
			appearance.set(cosmetic.category(), cosmetic.id());
		}
		RobloxifyData.get().markDirty();
		broadcastAppearance(player);
		play(player, RobloxifySounds.AVATAR_EQUIP);
		syncProfile(player);
	}

	public static void buy(ServerPlayer player, String cosmeticId) {
		Cosmetic cosmetic = CosmeticCatalog.byId(cosmeticId);
		if (cosmetic == null) {
			return;
		}
		Profile profile = profile(player);
		if (profile.owns(cosmeticId)) {
			notify(player, "info", "Already owned", cosmetic.name(), 0);
			return;
		}
		boolean free = player.isCreative();
		if (!free && profile.robux < cosmetic.price()) {
			play(player, RobloxifySounds.FAIL, 1.2F);
			notify(player, "fail", "Not enough Robux",
					cosmetic.name() + " costs " + com.robloxify.util.Robux.format(cosmetic.price()), 0);
			return;
		}
		if (!free) {
			profile.robux -= cosmetic.price();
		}
		profile.owned.add(cosmetic.id());
		RobloxifyData.get().markDirty();
		play(player, RobloxifySounds.PURCHASE);
		notify(player, "purchase", "New Avatar Item!", cosmetic.name(), 0);
		syncProfile(player);
		checkWealthBadges(player);
	}

	// -------------------------------------------------------------- experiences

	public static void startExperience(ServerPlayer player, String experienceId) {
		ExperienceType type = ExperienceType.byId(experienceId);
		if (type == null) {
			notify(player, "fail", "Unknown Experience", experienceId, 0);
			return;
		}
		if (type == ExperienceType.OBBY) {
			grantBadge(player, Badges.OBBY_BEGINNER);
		}
		ExperienceManager.start(player, type);
	}

	public static void onExperienceCompleted(ServerPlayer player, ExperienceType type, long elapsedMillis) {
		Profile profile = profile(player);
		if (!profile.completedExperiences.contains(type.id())) {
			profile.completedExperiences.add(type.id());
			RobloxifyData.get().markDirty();
		}
		grantBadge(player, Badges.FIRST_EXPERIENCE);
		checkExplorerBadge(player);
		if (type == ExperienceType.OBBY && elapsedMillis > 0 && elapsedMillis < 60_000L) {
			grantBadge(player, Badges.SPEEDRUNNER);
		}
	}

	// ------------------------------------------------------------------ actions

	public static void handleAction(ServerPlayer player, String action, String arg) {
		switch (action) {
			case "avatar_toggle" -> setAvatar(player, !profile(player).robloxAvatar);
			case "avatar_on" -> setAvatar(player, true);
			case "avatar_off" -> setAvatar(player, false);
			case "buy" -> buy(player, arg);
			case "equip" -> equip(player, arg);
			case "emote" -> emote(player, arg);
			case "experience_start" -> startExperience(player, arg);
			case "experience_stop" -> ExperienceManager.stop(player, false);
			default -> {
			}
		}
	}

	public static void emote(ServerPlayer player, String name) {
		if (!RobloxifyConfig.get().emotes) {
			return;
		}
		String emote = Emotes.normalise(name);
		if (!Emotes.isValid(emote)) {
			notify(player, "fail", "Unknown emote", name, 0);
			return;
		}
		RobloxifyNetworking.broadcast(player.level().getServer(), new RobloxifyPayloads.Emote(player.getUUID(), emote));
	}
}

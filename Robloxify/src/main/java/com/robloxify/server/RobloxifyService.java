package com.robloxify.server;

import com.robloxify.badge.Badge;
import com.robloxify.badge.Badges;
import com.robloxify.config.RobloxifyConfig;
import com.robloxify.data.Profile;
import com.robloxify.data.RobloxifyData;
import com.robloxify.experience.ObbyManager;
import com.robloxify.net.RobloxifyNetworking;
import com.robloxify.net.RobloxifyPayloads;
import com.robloxify.shop.Shop;
import com.robloxify.shop.ShopItem;
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
	private RobloxifyService() {
	}

	public static Profile profile(ServerPlayer player) {
		return RobloxifyData.get().profile(player.getUUID());
	}

	public static void syncProfile(ServerPlayer player) {
		Profile profile = profile(player);
		RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Profile(
				profile.robux, profile.blocksMined, profile.blocksPlaced, profile.robloxAvatar,
				profile.obbiesCompleted, profile.obbyBestMillis,
				new LinkedHashMap<>(profile.badges), new ArrayList<>(profile.owned)));
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
		RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.BadgeUnlocked(badge.id(), badge.reward()));
		player.sendSystemMessage(Component.literal("[Robloxify] ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(badge.icon() + " " + badge.name()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" unlocked  +" + badge.reward() + " Robux").withStyle(ChatFormatting.GREEN)));
		play(player, RobloxifySounds.BADGE_UNLOCK);
		syncProfile(player);
	}

	public static void addRobux(ServerPlayer player, int amount) {
		Profile profile = profile(player);
		profile.robux = Math.max(0, profile.robux + amount);
		RobloxifyData.get().markDirty();
		syncProfile(player);
		player.sendSystemMessage(Component.literal("[Robloxify] " + (amount >= 0 ? "+" : "") + amount
				+ " Robux (total " + profile.robux + ")").withStyle(ChatFormatting.YELLOW));
	}

	public static void setRobux(ServerPlayer player, int amount) {
		Profile profile = profile(player);
		profile.robux = Math.max(0, amount);
		RobloxifyData.get().markDirty();
		syncProfile(player);
		player.sendSystemMessage(Component.literal("[Robloxify] Robux set to " + profile.robux).withStyle(ChatFormatting.YELLOW));
	}

	public static void setAvatar(ServerPlayer player, boolean roblox) {
		if (!RobloxifyConfig.get().robloxAvatar) {
			player.sendSystemMessage(Component.literal("[Robloxify] The Roblox avatar is disabled in the config.")
					.withStyle(ChatFormatting.RED));
			return;
		}
		Profile profile = profile(player);
		if (profile.robloxAvatar == roblox) {
			return;
		}
		profile.robloxAvatar = roblox;
		RobloxifyData.get().markDirty();
		RobloxifyNetworking.broadcast(player.level().getServer(), new RobloxifyPayloads.Avatar(player.getUUID(), roblox));
		play(player, RobloxifySounds.AVATAR_SWITCH);
		if (roblox) {
			grantBadge(player, Badges.ROBLOXIAN);
		}
		checkEnderRobloxian(player);
		syncProfile(player);
		player.sendSystemMessage(Component.literal("[Robloxify] Avatar: " + (roblox ? "Roblox" : "Minecraft"))
				.withStyle(ChatFormatting.AQUA));
	}

	public static void checkEnderRobloxian(ServerPlayer player) {
		if (profile(player).robloxAvatar && player.level().dimension() == Level.END) {
			grantBadge(player, Badges.ENDER_ROBLOXIAN);
		}
	}

	public static void handleAction(ServerPlayer player, String action, String arg) {
		switch (action) {
			case "avatar_toggle" -> setAvatar(player, !profile(player).robloxAvatar);
			case "avatar_on" -> setAvatar(player, true);
			case "avatar_off" -> setAvatar(player, false);
			case "buy" -> buy(player, arg);
			case "emote" -> emote(player, arg);
			case "obby_start" -> ObbyManager.start(player);
			case "obby_stop" -> ObbyManager.stop(player, false);
			default -> {
			}
		}
	}

	public static void buy(ServerPlayer player, String id) {
		ShopItem item = Shop.byId(id);
		if (item == null) {
			return;
		}
		Profile profile = profile(player);
		if (profile.owned.contains(item.id())) {
			player.sendSystemMessage(Component.literal("[Robloxify] You already own " + item.name() + ".")
					.withStyle(ChatFormatting.GRAY));
			return;
		}
		if (profile.robux < item.price()) {
			play(player, RobloxifySounds.OOF, 1.4F);
			player.sendSystemMessage(Component.literal("[Robloxify] Not enough Robux for " + item.name()
					+ " (" + item.price() + ").").withStyle(ChatFormatting.RED));
			return;
		}
		profile.robux -= item.price();
		profile.owned.add(item.id());
		RobloxifyData.get().markDirty();
		play(player, RobloxifySounds.PURCHASE);
		syncProfile(player);
		player.sendSystemMessage(Component.literal("[Robloxify] Purchased ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(item.name()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" for " + item.price() + " Robux.").withStyle(ChatFormatting.GREEN)));
	}

	public static void emote(ServerPlayer player, String name) {
		if (!RobloxifyConfig.get().emotes) {
			return;
		}
		String emote = Emotes.normalise(name);
		if (!Emotes.isValid(emote)) {
			player.sendSystemMessage(Component.literal("[Robloxify] Unknown emote: " + name).withStyle(ChatFormatting.RED));
			return;
		}
		RobloxifyNetworking.broadcast(player.level().getServer(), new RobloxifyPayloads.Emote(player.getUUID(), emote));
	}
}

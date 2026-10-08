package com.robloxify.net;

import com.robloxify.Robloxify;
import com.robloxify.avatar.AvatarAppearance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** All custom packets used by Robloxify. */
public final class RobloxifyPayloads {
	private static final int MAX_ID = 64;

	private RobloxifyPayloads() {
	}

	private static void writeAppearance(RegistryFriendlyByteBuf buf, AvatarAppearance appearance) {
		buf.writeUtf(appearance.face, MAX_ID);
		buf.writeUtf(appearance.shirt, MAX_ID);
		buf.writeUtf(appearance.pants, MAX_ID);
		buf.writeUtf(appearance.hat, MAX_ID);
		buf.writeUtf(appearance.accessory, MAX_ID);
		buf.writeUtf(appearance.effect, MAX_ID);
		buf.writeUtf(appearance.animation, MAX_ID);
	}

	private static AvatarAppearance readAppearance(RegistryFriendlyByteBuf buf) {
		AvatarAppearance appearance = new AvatarAppearance();
		appearance.face = buf.readUtf(MAX_ID);
		appearance.shirt = buf.readUtf(MAX_ID);
		appearance.pants = buf.readUtf(MAX_ID);
		appearance.hat = buf.readUtf(MAX_ID);
		appearance.accessory = buf.readUtf(MAX_ID);
		appearance.effect = buf.readUtf(MAX_ID);
		appearance.animation = buf.readUtf(MAX_ID);
		return appearance;
	}

	/** Full profile sync, sent on join and whenever something changes. */
	public record Profile(int robux, int blocksMined, int blocksPlaced, boolean robloxAvatar,
						  int obbiesCompleted, int experiencesPlayed, long obbyBestMillis,
						  Map<String, Long> badges, List<String> owned,
						  AvatarAppearance appearance) implements CustomPacketPayload {
		public static final Type<Profile> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("profile"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Profile> CODEC =
				CustomPacketPayload.codec(Profile::write, Profile::read);

		public static Profile read(RegistryFriendlyByteBuf buf) {
			int robux = buf.readVarInt();
			int mined = buf.readVarInt();
			int placed = buf.readVarInt();
			boolean avatar = buf.readBoolean();
			int obbies = buf.readVarInt();
			int experiences = buf.readVarInt();
			long best = buf.readLong();
			int badgeCount = buf.readVarInt();
			Map<String, Long> badges = new LinkedHashMap<>();
			for (int i = 0; i < badgeCount; i++) {
				badges.put(buf.readUtf(MAX_ID), buf.readLong());
			}
			int ownedCount = buf.readVarInt();
			List<String> owned = new ArrayList<>(ownedCount);
			for (int i = 0; i < ownedCount; i++) {
				owned.add(buf.readUtf(MAX_ID));
			}
			AvatarAppearance appearance = readAppearance(buf);
			return new Profile(robux, mined, placed, avatar, obbies, experiences, best, badges, owned, appearance);
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeVarInt(robux);
			buf.writeVarInt(blocksMined);
			buf.writeVarInt(blocksPlaced);
			buf.writeBoolean(robloxAvatar);
			buf.writeVarInt(obbiesCompleted);
			buf.writeVarInt(experiencesPlayed);
			buf.writeLong(obbyBestMillis);
			buf.writeVarInt(badges.size());
			badges.forEach((id, time) -> {
				buf.writeUtf(id, MAX_ID);
				buf.writeLong(time);
			});
			buf.writeVarInt(owned.size());
			for (String id : owned) {
				buf.writeUtf(id, MAX_ID);
			}
			writeAppearance(buf, appearance);
		}

		@Override
		public Type<Profile> type() {
			return TYPE;
		}
	}

	/** Broadcast when a player switches look or changes cosmetics. */
	public record Appearance(UUID player, boolean roblox, AvatarAppearance appearance) implements CustomPacketPayload {
		public static final Type<Appearance> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("appearance"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Appearance> CODEC =
				CustomPacketPayload.codec(Appearance::write, Appearance::read);

		public static Appearance read(RegistryFriendlyByteBuf buf) {
			return new Appearance(buf.readUUID(), buf.readBoolean(), readAppearance(buf));
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeUUID(player);
			buf.writeBoolean(roblox);
			writeAppearance(buf, appearance);
		}

		@Override
		public Type<Appearance> type() {
			return TYPE;
		}
	}

	/** Roblox style platform notification. */
	public record Notification(String kind, String title, String subtitle, int robuxReward) implements CustomPacketPayload {
		public static final Type<Notification> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("notification"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Notification> CODEC =
				CustomPacketPayload.codec(Notification::write, Notification::read);

		public static Notification read(RegistryFriendlyByteBuf buf) {
			return new Notification(buf.readUtf(24), buf.readUtf(96), buf.readUtf(160), buf.readVarInt());
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeUtf(kind, 24);
			buf.writeUtf(title, 96);
			buf.writeUtf(subtitle, 160);
			buf.writeVarInt(robuxReward);
		}

		@Override
		public Type<Notification> type() {
			return TYPE;
		}
	}

	/** Emote broadcast. */
	public record Emote(UUID player, String emote) implements CustomPacketPayload {
		public static final Type<Emote> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("emote"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Emote> CODEC =
				CustomPacketPayload.codec(Emote::write, Emote::read);

		public static Emote read(RegistryFriendlyByteBuf buf) {
			return new Emote(buf.readUUID(), buf.readUtf(32));
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeUUID(player);
			buf.writeUtf(emote, 32);
		}

		@Override
		public Type<Emote> type() {
			return TYPE;
		}
	}

	/** Live state of the Experience the player is currently in. */
	public record Experience(boolean active, String experienceId, int checkpoint, int total,
							 long elapsedMillis, boolean completed, String objective) implements CustomPacketPayload {
		public static final Type<Experience> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("experience"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Experience> CODEC =
				CustomPacketPayload.codec(Experience::write, Experience::read);

		public static Experience read(RegistryFriendlyByteBuf buf) {
			return new Experience(buf.readBoolean(), buf.readUtf(32), buf.readVarInt(), buf.readVarInt(),
					buf.readLong(), buf.readBoolean(), buf.readUtf(96));
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeBoolean(active);
			buf.writeUtf(experienceId, 32);
			buf.writeVarInt(checkpoint);
			buf.writeVarInt(total);
			buf.writeLong(elapsedMillis);
			buf.writeBoolean(completed);
			buf.writeUtf(objective, 96);
		}

		@Override
		public Type<Experience> type() {
			return TYPE;
		}
	}

	/** Generic client to server action, used by the Robloxify UI. */
	public record Action(String action, String arg) implements CustomPacketPayload {
		public static final Type<Action> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("action"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Action> CODEC =
				CustomPacketPayload.codec(Action::write, Action::read);

		public static Action read(RegistryFriendlyByteBuf buf) {
			return new Action(buf.readUtf(32), buf.readUtf(64));
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeUtf(action, 32);
			buf.writeUtf(arg, 64);
		}

		@Override
		public Type<Action> type() {
			return TYPE;
		}
	}
}

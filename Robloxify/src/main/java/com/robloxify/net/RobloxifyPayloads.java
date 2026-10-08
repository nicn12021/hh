package com.robloxify.net;

import com.robloxify.Robloxify;
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
	private RobloxifyPayloads() {
	}

	/** Full profile sync, sent on join and whenever something changes. */
	public record Profile(int robux, int blocksMined, int blocksPlaced, boolean robloxAvatar,
						  int obbiesCompleted, long obbyBestMillis, Map<String, Long> badges,
						  List<String> owned) implements CustomPacketPayload {
		public static final Type<Profile> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("profile"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Profile> CODEC =
				CustomPacketPayload.codec(Profile::write, Profile::read);

		@Override
		public Type<Profile> type() {
			return TYPE;
		}

		public static Profile read(RegistryFriendlyByteBuf buf) {
			int robux = buf.readVarInt();
			int mined = buf.readVarInt();
			int placed = buf.readVarInt();
			boolean avatar = buf.readBoolean();
			int obbies = buf.readVarInt();
			long best = buf.readLong();
			int badgeCount = buf.readVarInt();
			Map<String, Long> badges = new LinkedHashMap<>();
			for (int i = 0; i < badgeCount; i++) {
				badges.put(buf.readUtf(64), buf.readLong());
			}
			int ownedCount = buf.readVarInt();
			List<String> owned = new ArrayList<>(ownedCount);
			for (int i = 0; i < ownedCount; i++) {
				owned.add(buf.readUtf(64));
			}
			return new Profile(robux, mined, placed, avatar, obbies, best, badges, owned);
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeVarInt(robux);
			buf.writeVarInt(blocksMined);
			buf.writeVarInt(blocksPlaced);
			buf.writeBoolean(robloxAvatar);
			buf.writeVarInt(obbiesCompleted);
			buf.writeLong(obbyBestMillis);
			buf.writeVarInt(badges.size());
			badges.forEach((id, time) -> {
				buf.writeUtf(id, 64);
				buf.writeLong(time);
			});
			buf.writeVarInt(owned.size());
			for (String id : owned) {
				buf.writeUtf(id, 64);
			}
		}
	}

	/** Broadcast when a player switches between the vanilla and Roblox look. */
	public record Avatar(UUID player, boolean roblox) implements CustomPacketPayload {
		public static final Type<Avatar> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("avatar"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Avatar> CODEC =
				CustomPacketPayload.codec(Avatar::write, Avatar::read);

		@Override
		public Type<Avatar> type() {
			return TYPE;
		}

		public static Avatar read(RegistryFriendlyByteBuf buf) {
			return new Avatar(buf.readUUID(), buf.readBoolean());
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeUUID(player);
			buf.writeBoolean(roblox);
		}
	}

	/** Broadcast when a player plays an emote. */
	public record Emote(UUID player, String emote) implements CustomPacketPayload {
		public static final Type<Emote> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("emote"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Emote> CODEC =
				CustomPacketPayload.codec(Emote::write, Emote::read);

		@Override
		public Type<Emote> type() {
			return TYPE;
		}

		public static Emote read(RegistryFriendlyByteBuf buf) {
			return new Emote(buf.readUUID(), buf.readUtf(32));
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeUUID(player);
			buf.writeUtf(emote, 32);
		}
	}

	/** Sent when a badge is unlocked so the client can play a toast + sound. */
	public record BadgeUnlocked(String badgeId, int robuxReward) implements CustomPacketPayload {
		public static final Type<BadgeUnlocked> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("badge"));
		public static final StreamCodec<RegistryFriendlyByteBuf, BadgeUnlocked> CODEC =
				CustomPacketPayload.codec(BadgeUnlocked::write, BadgeUnlocked::read);

		@Override
		public Type<BadgeUnlocked> type() {
			return TYPE;
		}

		public static BadgeUnlocked read(RegistryFriendlyByteBuf buf) {
			return new BadgeUnlocked(buf.readUtf(64), buf.readVarInt());
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeUtf(badgeId, 64);
			buf.writeVarInt(robuxReward);
		}
	}

	/** Obby (Experience) HUD state. */
	public record Obby(boolean active, int checkpoint, int total, long elapsedMillis, boolean completed) implements CustomPacketPayload {
		public static final Type<Obby> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("obby"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Obby> CODEC =
				CustomPacketPayload.codec(Obby::write, Obby::read);

		@Override
		public Type<Obby> type() {
			return TYPE;
		}

		public static Obby read(RegistryFriendlyByteBuf buf) {
			return new Obby(buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readLong(), buf.readBoolean());
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeBoolean(active);
			buf.writeVarInt(checkpoint);
			buf.writeVarInt(total);
			buf.writeLong(elapsedMillis);
			buf.writeBoolean(completed);
		}
	}

	/** Generic client to server action, used by the Roblox UI. */
	public record Action(String action, String arg) implements CustomPacketPayload {
		public static final Type<Action> TYPE = new CustomPacketPayload.Type<>(Robloxify.id("action"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Action> CODEC =
				CustomPacketPayload.codec(Action::write, Action::read);

		@Override
		public Type<Action> type() {
			return TYPE;
		}

		public static Action read(RegistryFriendlyByteBuf buf) {
			return new Action(buf.readUtf(32), buf.readUtf(64));
		}

		public void write(RegistryFriendlyByteBuf buf) {
			buf.writeUtf(action, 32);
			buf.writeUtf(arg, 64);
		}
	}
}

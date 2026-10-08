package com.robloxify.experience;

import com.robloxify.badge.Badges;
import com.robloxify.config.RobloxifyConfig;
import com.robloxify.data.RobloxifyData;
import com.robloxify.net.RobloxifyNetworking;
import com.robloxify.net.RobloxifyPayloads;
import com.robloxify.server.RobloxifyService;
import com.robloxify.sound.RobloxifySounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Obby Experience. Platforms are plain vanilla blocks, and only players with an active
 * session are ever inspected, so the tick cost is effectively zero while nobody is playing.
 */
public final class ObbyManager {
	public static final int PLATFORMS = 8;
	private static final int SPACING = 4;
	private static final long MAX_MILLIS = 15L * 60L * 1000L;
	private static final double MAX_DISTANCE_SQ = 96.0 * 96.0;
	private static final Map<UUID, Session> SESSIONS = new HashMap<>();

	private ObbyManager() {
	}

	public static final class Session {
		public final UUID playerId;
		public final long startMillis;
		public final List<BlockPos> checkpoints;
		public final int startY;
		public int next;
		public boolean completed;

		Session(UUID playerId, long startMillis, List<BlockPos> checkpoints, int startY) {
			this.playerId = playerId;
			this.startMillis = startMillis;
			this.checkpoints = checkpoints;
			this.startY = startY;
		}
	}

	public static boolean isActive(UUID playerId) {
		return SESSIONS.containsKey(playerId);
	}

	public static void start(ServerPlayer player) {
		if (!RobloxifyConfig.get().experiences) {
			player.sendSystemMessage(Component.literal("[Robloxify] Experiences are disabled in the config.")
					.withStyle(ChatFormatting.RED));
			return;
		}
		stop(player, true);

		ServerLevel level = player.level();
		Direction facing = player.getDirection();
		BlockPos cursor = player.blockPosition().relative(facing, 4);
		List<BlockPos> checkpoints = new ArrayList<>();
		BlockState platform = Blocks.STONE_BRICKS.defaultBlockState();
		BlockState checkpoint = Blocks.GOLD_BLOCK.defaultBlockState();

		for (int i = 0; i < PLATFORMS; i++) {
			boolean isCheckpoint = i == 2 || i == 5 || i == PLATFORMS - 1;
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos pos = cursor.offset(dx, 0, dz);
					level.setBlockAndUpdate(pos, isCheckpoint && dx == 0 && dz == 0 ? checkpoint : platform);
					level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
					level.setBlockAndUpdate(pos.above(2), Blocks.AIR.defaultBlockState());
				}
			}
			if (isCheckpoint) {
				checkpoints.add(cursor.immutable());
			}
			cursor = cursor.relative(facing, SPACING).above(1);
		}

		Session session = new Session(player.getUUID(), System.currentTimeMillis(), checkpoints, player.blockPosition().getY());
		SESSIONS.put(player.getUUID(), session);
		send(player, session);
		RobloxifyService.play(player, RobloxifySounds.CHECKPOINT);
		player.sendSystemMessage(Component.literal("[Robloxify] ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal("Obby").withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" started! Reach all " + checkpoints.size() + " checkpoints.")
						.withStyle(ChatFormatting.WHITE)));
	}

	public static void stop(ServerPlayer player, boolean silent) {
		Session session = SESSIONS.remove(player.getUUID());
		if (session == null) {
			return;
		}
		RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Obby(false, 0, session.checkpoints.size(), 0L, false));
		if (!silent) {
			player.sendSystemMessage(Component.literal("[Robloxify] Obby stopped.").withStyle(ChatFormatting.GRAY));
		}
	}

	public static void tick(MinecraftServer server) {
		if (SESSIONS.isEmpty()) {
			return;
		}
		long now = System.currentTimeMillis();
		Iterator<Map.Entry<UUID, Session>> iterator = SESSIONS.entrySet().iterator();
		while (iterator.hasNext()) {
			Session session = iterator.next().getValue();
			ServerPlayer player = server.getPlayerList().getPlayer(session.playerId);
			if (player == null) {
				iterator.remove();
				continue;
			}
			if (now - session.startMillis > MAX_MILLIS) {
				iterator.remove();
				RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Obby(false, 0, session.checkpoints.size(), 0L, false));
				player.sendSystemMessage(Component.literal("[Robloxify] Obby timed out.").withStyle(ChatFormatting.GRAY));
				continue;
			}

			BlockPos first = session.checkpoints.get(0);
			if (player.getX() * player.getX() + player.getZ() * player.getZ() > 0
					&& player.blockPosition().distSqr(first) > MAX_DISTANCE_SQ) {
				stop(player, false);
				iterator.remove();
				continue;
			}

			if (player.getY() < session.startY - 25.0) {
				BlockPos respawn = session.next == 0 ? first : session.checkpoints.get(session.next - 1);
				player.teleportTo(respawn.getX() + 0.5, respawn.getY() + 1.0, respawn.getZ() + 0.5);
				player.sendSystemMessage(Component.literal("[Robloxify] Back to the last checkpoint!")
						.withStyle(ChatFormatting.YELLOW));
				continue;
			}

			if (session.completed || session.next >= session.checkpoints.size()) {
				continue;
			}

			BlockPos target = session.checkpoints.get(session.next);
			double dx = player.getX() - (target.getX() + 0.5);
			double dz = player.getZ() - (target.getZ() + 0.5);
			double dy = player.getY() - target.getY();
			if (dx * dx + dz * dz <= 2.25 && dy >= -1.5 && dy <= 2.5) {
				session.next++;
				if (session.next >= session.checkpoints.size()) {
					complete(player, session);
					iterator.remove();
				} else {
					RobloxifyService.play(player, RobloxifySounds.CHECKPOINT, 1.2F);
					send(player, session);
					player.sendSystemMessage(Component.literal("[Robloxify] Checkpoint " + session.next + "/"
							+ session.checkpoints.size()).withStyle(ChatFormatting.YELLOW));
				}
			}
		}
	}

	private static void complete(ServerPlayer player, Session session) {
		long elapsed = System.currentTimeMillis() - session.startMillis;
		var profile = RobloxifyService.profile(player);
		profile.obbiesCompleted++;
		if (profile.obbyBestMillis == 0L || elapsed < profile.obbyBestMillis) {
			profile.obbyBestMillis = elapsed;
		}
		RobloxifyData.get().markDirty();
		RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Obby(false, session.checkpoints.size(),
				session.checkpoints.size(), elapsed, true));
		RobloxifyService.play(player, RobloxifySounds.EXPERIENCE_COMPLETE);
		player.sendSystemMessage(Component.literal("[Robloxify] ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal("Obby complete!").withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Time: " + (elapsed / 1000.0) + "s").withStyle(ChatFormatting.WHITE)));
		ServerLevel level = player.level();
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(),
				40, 1.0, 1.0, 1.0, 0.1);
		RobloxifyService.grantBadge(player, Badges.OBBY_SURVIVOR);
		RobloxifyService.addRobux(player, 250);
	}

	private static void send(ServerPlayer player, Session session) {
		RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Obby(true, session.next,
				session.checkpoints.size(), System.currentTimeMillis() - session.startMillis, false));
	}
}

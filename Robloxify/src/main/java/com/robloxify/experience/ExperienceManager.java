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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Runs every Robloxify Experience.
 * <p>
 * The tick loop returns immediately when nobody is playing, and only ever inspects the players who
 * actually have an active session, so the cost outside of an Experience is zero.
 */
public final class ExperienceManager {
	public static final long TIMEOUT_MILLIS = 15L * 60L * 1000L;
	private static final long RACE_LIMIT_MILLIS = 90_000L;
	private static final long MINIGAME_LIMIT_MILLIS = 120_000L;

	private static final Map<UUID, Session> SESSIONS = new HashMap<>();
	private static final Map<BlockPos, Restore> VANISHING = new HashMap<>();

	private ExperienceManager() {
	}

	public static final class Session {
		public final UUID playerId;
		public final ExperienceType type;
		public final long startMillis;
		public final ArenaBuilder.Built arena;
		public final ServerLevel level;
		public int next;
		public int wave;
		public int score;
		public int earned;
		public long lastAction;
		public long lastIncome;
		public long lastWaveSpawn;
		public boolean completed;
		public String objective = "";

		Session(UUID playerId, ExperienceType type, ArenaBuilder.Built arena, ServerLevel level) {
			this.playerId = playerId;
			this.type = type;
			this.arena = arena;
			this.level = level;
			this.startMillis = System.currentTimeMillis();
			this.lastAction = this.startMillis;
			this.lastIncome = this.startMillis;
		}
	}

	private record Restore(BlockState state, long restoreAt) {
	}

	// ---------------------------------------------------------------- queries

	public static Session sessionOf(ServerPlayer player) {
		return SESSIONS.get(player.getUUID());
	}

	public static boolean isPlaying(UUID playerId) {
		return SESSIONS.containsKey(playerId);
	}

	// ------------------------------------------------------------------ start

	public static void start(ServerPlayer player, ExperienceType type) {
		if (!RobloxifyConfig.get().experiences) {
			RobloxifyService.notify(player, "fail", "Experiences disabled",
					"Enable the experience system in Settings.", 0);
			return;
		}
		stop(player, true);

		ServerLevel level = player.level();
		Direction facing = player.getDirection();
		BlockPos origin = player.blockPosition().relative(facing, 3).below();
		ArenaBuilder.Built arena = ArenaBuilder.build(level, type, origin, facing);

		Session session = new Session(player.getUUID(), type, arena, level);
		session.score = 0;
		SESSIONS.put(player.getUUID(), session);

		player.teleportTo(arena.spawn.getX() + 0.5, arena.spawn.getY(), arena.spawn.getZ() + 0.5);
		player.setDeltaMovement(0.0, 0.0, 0.0);
		player.hurtMarked = true;

		var profile = RobloxifyService.profile(player);
		profile.experiencesPlayed++;
		RobloxifyData.get().markDirty();

		RobloxifyService.play(player, RobloxifySounds.EXPERIENCE_START);
		level.sendParticles(ParticleTypes.END_ROD, arena.spawn.getX() + 0.5, arena.spawn.getY() + 0.5,
				arena.spawn.getZ() + 0.5, 30, 1.0, 1.0, 1.0, 0.05);
		RobloxifyService.notify(player, "experience", type.displayName(), type.description(), 0);
		sendState(player, session);

		if (type == ExperienceType.SWORD_ARENA || type == ExperienceType.SURVIVAL) {
			session.lastWaveSpawn = System.currentTimeMillis();
		}
	}

	public static void stop(ServerPlayer player, boolean silent) {
		Session session = SESSIONS.remove(player.getUUID());
		if (session == null) {
			return;
		}
		RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Experience(false, session.type.id(), 0,
				session.arena.total, 0L, false, ""));
		if (!silent) {
			RobloxifyService.notify(player, "info", "Experience left", session.type.displayName(), 0);
		}
	}

	// --------------------------------------------------------------- gameplay

	public static void onCheckpoint(ServerPlayer player, BlockPos pos) {
		Session session = SESSIONS.get(player.getUUID());
		if (session == null || session.completed) {
			return;
		}
		List<BlockPos> checkpoints = session.arena.checkpoints;
		if (session.next >= checkpoints.size()) {
			return;
		}
		BlockPos expected = checkpoints.get(session.next);
		if (!expected.equals(pos)) {
			return;
		}
		session.next++;
		session.lastAction = System.currentTimeMillis();
		RobloxifyService.play(player, RobloxifySounds.CHECKPOINT, 1.0F + session.next * 0.05F);
		session.level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.0,
				pos.getZ() + 0.5, 18, 0.6, 0.4, 0.6, 0.05);
		RobloxifyService.notify(player, "checkpoint", "Checkpoint reached",
				session.next + " / " + checkpoints.size(), 0);

		if (session.next >= checkpoints.size() && session.type != ExperienceType.OBBY) {
			complete(player, session);
		} else {
			sendState(player, session);
		}
	}

	public static void onFinishPad(ServerPlayer player, BlockPos pos) {
		Session session = SESSIONS.get(player.getUUID());
		if (session == null || session.completed) {
			return;
		}
		complete(player, session);
	}

	public static void scheduleVanish(ServerLevel level, BlockPos pos, BlockState state) {
		if (VANISHING.containsKey(pos) || level.getBlockState(pos).isAir()) {
			return;
		}
		long now = System.currentTimeMillis();
		VANISHING.put(pos, new Restore(state, now + 900L));
		level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
	}

	/** Called when a player dies. Resets them to the Experience spawn or checkpoint. */
	public static void onPlayerDeath(ServerPlayer player) {
		Session session = SESSIONS.get(player.getUUID());
		if (session == null) {
			return;
		}
		session.lastAction = System.currentTimeMillis();
		RobloxifyService.notify(player, "fail", "You died!", "Resetting to " + session.type.displayName(), 0);
	}

	// ------------------------------------------------------------------- tick

	public static void tick(MinecraftServer server) {
		tickVanishing();
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
			if (session.completed) {
				iterator.remove();
				continue;
			}
			if (now - session.startMillis > TIMEOUT_MILLIS) {
				iterator.remove();
				RobloxifyService.notify(player, "fail", "Experience timed out", session.type.displayName(), 0);
				RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Experience(false, session.type.id(),
						session.next, session.arena.total, 0L, false, ""));
				continue;
			}

			// Falling out of the world always resets to the last checkpoint.
			if (player.getY() < session.arena.boundsMin.getY() - 16) {
				resetToCheckpoint(player, session);
				continue;
			}

			switch (session.type) {
				case OBBY, RACE -> {
					if (session.type == ExperienceType.RACE
							&& now - session.startMillis > RACE_LIMIT_MILLIS) {
						iterator.remove();
						RobloxifyService.notify(player, "fail", "Time up!", "Race failed", 0);
						RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Experience(false,
								session.type.id(), session.next, session.arena.total, 0L, false, ""));
					}
				}
				case SWORD_ARENA, SURVIVAL -> tickWaves(player, session, now);
				case TYCOON -> tickTycoon(player, session, now);
				case MINIGAME -> tickMinigame(player, session, now);
			}

			if (now - session.lastAction > 4000L) {
				session.lastAction = now;
				sendState(player, session);
			}
		}
	}

	private static void tickVanishing() {
		if (VANISHING.isEmpty()) {
			return;
		}
		long now = System.currentTimeMillis();
		Iterator<Map.Entry<BlockPos, Restore>> iterator = VANISHING.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<BlockPos, Restore> entry = iterator.next();
			if (entry.getValue().restoreAt() > now) {
				continue;
			}
			iterator.remove();
		}
	}

	private static void tickWaves(ServerPlayer player, Session session, long now) {
		AABB box = new AABB(session.arena.boundsMin.getX(), session.arena.boundsMin.getY(),
				session.arena.boundsMin.getZ(), session.arena.boundsMax.getX() + 1,
				session.arena.boundsMax.getY() + 1, session.arena.boundsMax.getZ() + 1);
		int alive = session.level.getEntitiesOfClass(Monster.class, box, Monster::isAlive).size();

		if (alive > 0) {
			session.objective = "Wave " + (session.wave + 1) + "/3 - " + alive + " enemies left";
			return;
		}
		if (now - session.lastWaveSpawn < 2500L) {
			return;
		}
		if (session.wave >= 3) {
			complete(player, session);
			return;
		}
		session.wave++;
		session.lastWaveSpawn = now;
		spawnWave(session);
		session.objective = "Wave " + session.wave + "/3";
		sendState(player, session);
		RobloxifyService.notify(player, "experience", "Wave " + session.wave, "Enemies incoming!", 0);
	}

	private static void spawnWave(Session session) {
		ServerLevel level = session.level;
		BlockPos center = session.arena.spawn;
		int count = 2 + session.wave;
		for (int i = 0; i < count; i++) {
			double angle = (Math.PI * 2 / count) * i;
			int x = center.getX() + (int) Math.round(Math.cos(angle) * 5);
			int z = center.getZ() + (int) Math.round(Math.sin(angle) * 5);
			BlockPos pos = new BlockPos(x, center.getY(), z);
			if (session.type == ExperienceType.SURVIVAL) {
				if (i % 3 == 0) {
					EntityTypes.CREEPER.spawn(level, pos, EntitySpawnReason.EVENT);
				} else {
					EntityTypes.HUSK.spawn(level, pos, EntitySpawnReason.EVENT);
				}
			} else {
				if (i % 2 == 0) {
					EntityTypes.SKELETON.spawn(level, pos, EntitySpawnReason.EVENT);
				} else {
					EntityTypes.CAVE_SPIDER.spawn(level, pos, EntitySpawnReason.EVENT);
				}
			}
		}
	}

	private static void tickTycoon(ServerPlayer player, Session session, long now) {
		if (now - session.lastIncome < 2000L) {
			return;
		}
		session.lastIncome = now;
		if (!insideArena(player, session)) {
			session.objective = "Return to your plot to earn";
			return;
		}
		session.earned++;
		session.objective = "Tycoon income " + session.earned + "/100";
		if (session.earned % 20 == 0) {
			RobloxifyService.addRobux(player, 5);
			session.level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0,
					player.getZ(), 6, 0.4, 0.4, 0.4, 0.02);
		}
		if (session.earned >= 100) {
			complete(player, session);
		}
	}

	private static void tickMinigame(ServerPlayer player, Session session, long now) {
		int remaining = 0;
		for (BlockPos target : session.arena.targets) {
			if (!session.level.getBlockState(target).isAir()) {
				remaining++;
			}
		}
		if (remaining != session.score) {
			session.score = remaining;
			int done = session.arena.targets.size() - remaining;
			session.objective = "Targets " + done + "/" + session.arena.targets.size();
			sendState(player, session);
		}
		if (remaining == 0) {
			complete(player, session);
			return;
		}
		if (now - session.startMillis > MINIGAME_LIMIT_MILLIS) {
			RobloxifyService.notify(player, "fail", "Time up!", "Minigame failed", 0);
			RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Experience(false, session.type.id(),
					session.score, session.arena.total, 0L, false, ""));
			SESSIONS.remove(player.getUUID());
		}
	}

	private static boolean insideArena(ServerPlayer player, Session session) {
		BlockPos pos = player.blockPosition();
		return pos.getX() >= session.arena.boundsMin.getX() && pos.getX() <= session.arena.boundsMax.getX()
				&& pos.getZ() >= session.arena.boundsMin.getZ() && pos.getZ() <= session.arena.boundsMax.getZ()
				&& pos.getY() >= session.arena.boundsMin.getY() && pos.getY() <= session.arena.boundsMax.getY();
	}

	private static void resetToCheckpoint(ServerPlayer player, Session session) {
		BlockPos target = session.arena.spawn;
		if (!session.arena.checkpoints.isEmpty() && session.next > 0) {
			target = session.arena.checkpoints.get(Math.min(session.next - 1, session.arena.checkpoints.size() - 1));
		}
		player.teleportTo(target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5);
		player.setDeltaMovement(0.0, 0.0, 0.0);
		player.hurtMarked = true;
		RobloxifyService.play(player, RobloxifySounds.RESPAWN);
		RobloxifyService.notify(player, "checkpoint", "Reset", "Back to the last checkpoint", 0);
	}

	// ---------------------------------------------------------------- complete

	private static void complete(ServerPlayer player, Session session) {
		session.completed = true;
		SESSIONS.remove(player.getUUID());
		long elapsed = System.currentTimeMillis() - session.startMillis;

		var profile = RobloxifyService.profile(player);
		int reward = session.type.reward();
		if (session.type == ExperienceType.OBBY) {
			profile.obbiesCompleted++;
			if (profile.obbyBestMillis == 0L || elapsed < profile.obbyBestMillis) {
				profile.obbyBestMillis = elapsed;
			}
			RobloxifyService.grantBadge(player, Badges.OBBY_SURVIVOR);
		}
		RobloxifyData.get().markDirty();

		RobloxifyService.onExperienceCompleted(player, session.type, elapsed);
		RobloxifyService.addRobux(player, reward);
		RobloxifyService.play(player, RobloxifySounds.EXPERIENCE_COMPLETE);
		RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Experience(false, session.type.id(),
				session.arena.total, session.arena.total, elapsed, true, ""));
		RobloxifyService.notify(player, "complete", "Experience complete!",
				session.type.displayName() + "  in " + (elapsed / 1000.0) + "s", reward);

		session.level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0,
				player.getZ(), 60, 1.0, 1.0, 1.0, 0.3);
		player.sendSystemMessage(Component.literal("[Robloxify] ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(session.type.displayName() + " complete!").withStyle(ChatFormatting.GOLD)));
	}

	private static void sendState(ServerPlayer player, Session session) {
		RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Experience(true, session.type.id(),
				session.next, session.arena.total, System.currentTimeMillis() - session.startMillis,
				false, session.objective.isEmpty() ? session.type.description() : session.objective));
	}

	/** Experience blocks that were broken get restored, so arenas stay usable. */
	public static List<BlockPos> restoreList() {
		return new ArrayList<>(VANISHING.keySet());
	}
}

package com.robloxify.experience;

import com.robloxify.world.RobloxifyBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Builds the physical arena for each Experience out of ordinary Minecraft blocks. */
public final class ArenaBuilder {
	public static final class Built {
		public BlockPos spawn = BlockPos.ZERO;
		public final List<BlockPos> checkpoints = new ArrayList<>();
		public final List<BlockPos> targets = new ArrayList<>();
		public final List<Mover> movers = new ArrayList<>();
		public BlockPos boundsMin = BlockPos.ZERO;
		public BlockPos boundsMax = BlockPos.ZERO;
		public String objective = "";
		public int total = 1;
	}

	private ArenaBuilder() {
	}

	public static Built build(ServerLevel level, ExperienceType type, BlockPos origin, Direction facing) {
		return switch (type) {
			case OBBY -> obby(level, origin, facing);
			case SWORD_ARENA -> arena(level, origin, facing, "Clear the waves");
			case SURVIVAL -> arena(level, origin, facing, "Survive the waves");
			case RACE -> race(level, origin, facing);
			case TYCOON -> tycoon(level, origin, facing);
			case MINIGAME -> minigame(level, origin, facing);
		};
	}

	// ---------------------------------------------------------------- helpers

	private static void set(ServerLevel level, BlockPos pos, BlockState state) {
		level.setBlockAndUpdate(pos, state);
	}

	private static void slab(ServerLevel level, BlockPos center, int radius, BlockState state) {
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				set(level, center.offset(dx, 0, dz), state);
				for (int dy = 1; dy <= 4; dy++) {
					set(level, center.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
				}
			}
		}
	}

	private static void walls(ServerLevel level, BlockPos center, int radius, int height, BlockState state) {
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
					continue;
				}
				for (int dy = 0; dy < height; dy++) {
					set(level, center.offset(dx, dy, dz), state);
				}
			}
		}
	}

	private static final BlockState PLATFORM = Blocks.STONE_BRICKS.defaultBlockState();
	private static final BlockState ACCENT = Blocks.POLISHED_ANDESITE.defaultBlockState();

	// ------------------------------------------------------------------ OBBY

	private static Built obby(ServerLevel level, BlockPos origin, Direction facing) {
		Built built = new Built();
		BlockState spawnPad = RobloxifyBlocks.SPAWN_PAD.defaultBlockState();
		BlockState checkpoint = RobloxifyBlocks.CHECKPOINT.defaultBlockState();
		BlockState bounce = RobloxifyBlocks.BOUNCE_PAD.defaultBlockState();
		BlockState finish = RobloxifyBlocks.FINISH_PAD.defaultBlockState();
		BlockState vanish = RobloxifyBlocks.DISAPPEARING_PLATFORM.defaultBlockState();

		slab(level, origin, 1, spawnPad);
		built.spawn = origin.above();
		built.boundsMin = origin.offset(-24, -32, -24);
		built.boundsMax = origin.offset(24, 32, 24);

		BlockPos cursor = origin.relative(facing, 4);
		int checkpointIndex = 0;
		for (int step = 0; step < 11; step++) {
			boolean isCheckpoint = step == 3 || step == 6 || step == 9;
			boolean isBounce = step == 4 || step == 7;
			boolean isVanish = step == 2 || step == 5;
			boolean isMover = step == 8;

			if (isMover) {
				// A gap with a sliding platform instead of a static one.
				slab(level, cursor.relative(facing, -2), 1, PLATFORM);
				slab(level, cursor.relative(facing, 2), 1, PLATFORM);
				Mover mover = new Mover(cursor.offset(-1, 0, -1), facing.getClockWise(), 3, 30);
				built.movers.add(mover);
				paintMover(level, mover, ACCENT);
				cursor = cursor.relative(facing, 4).above(1);
				continue;
			}

			BlockState surface = isVanish ? vanish : PLATFORM;
			slab(level, cursor, 1, surface);

			if (isCheckpoint) {
				set(level, cursor, checkpoint);
				built.checkpoints.add(cursor.immutable());
				checkpointIndex++;
			} else if (isBounce) {
				set(level, cursor, bounce);
			}

			// A small hazard pit under the awkward jumps.
			if (step == 1 || step == 6) {
				BlockPos pit = cursor.relative(facing.getOpposite(), 2).below(3);
				slab(level, pit, 1, Blocks.LAVA.defaultBlockState());
			}

			cursor = cursor.relative(facing, 4).above(step % 2 == 0 ? 1 : 0);
		}

		slab(level, cursor, 1, finish);
		built.checkpoints.add(cursor.immutable());
		built.spawn = origin.above();
		built.total = built.checkpoints.size();
		built.objective = "Reach checkpoint 0/" + built.total;
		return built;
	}

	private static void paintMover(ServerLevel level, Mover mover, BlockState state) {
		BlockPos center = mover.current();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				set(level, center.offset(dx, 0, dz), state);
			}
		}
	}

	// ------------------------------------------------------- ARENA (combat)

	private static Built arena(ServerLevel level, BlockPos origin, Direction facing, String objective) {
		Built built = new Built();
		built.spawn = origin.above();
		built.boundsMin = origin.offset(-8, -4, -8);
		built.boundsMax = origin.offset(8, 12, 8);

		slab(level, origin, 7, Blocks.SMOOTH_STONE.defaultBlockState());
		for (int dx = -7; dx <= 7; dx++) {
			for (int dz = -7; dz <= 7; dz++) {
				if (Math.abs(dx) == 7 || Math.abs(dz) == 7) {
					set(level, origin.offset(dx, 0, dz), Blocks.STONE_BRICKS.defaultBlockState());
				}
			}
		}
		walls(level, origin, 7, 4, Blocks.STONE_BRICKS.defaultBlockState());
		slab(level, origin, 1, RobloxifyBlocks.SPAWN_PAD.defaultBlockState());

		built.total = 3;
		built.objective = objective + " (0/3)";
		return built;
	}

	// ------------------------------------------------------------------ RACE

	private static Built race(ServerLevel level, BlockPos origin, Direction facing) {
		Built built = new Built();
		BlockState pad = RobloxifyBlocks.CHECKPOINT.defaultBlockState();
		BlockState track = Blocks.POLISHED_ANDESITE.defaultBlockState();

		slab(level, origin, 1, RobloxifyBlocks.SPAWN_PAD.defaultBlockState());
		built.spawn = origin.above();
		built.boundsMin = origin.offset(-24, -8, -24);
		built.boundsMax = origin.offset(24, 16, 24);

		int radius = 12;
		int[][] corners = {{-radius, -radius}, {radius, -radius}, {radius, radius}, {-radius, radius}};
		for (int[] corner : corners) {
			BlockPos padPos = origin.offset(corner[0], 0, corner[1]);
			slab(level, padPos, 1, track);
			set(level, padPos, pad);
			built.checkpoints.add(padPos.immutable());
		}
		built.checkpoints.add(origin.immutable());
		built.total = built.checkpoints.size();
		built.objective = "Checkpoints 0/" + built.total;
		return built;
	}

	// ---------------------------------------------------------------- TYCOON

	private static Built tycoon(ServerLevel level, BlockPos origin, Direction facing) {
		Built built = new Built();
		slab(level, origin, 4, Blocks.OAK_PLANKS.defaultBlockState());
		for (int dx = -4; dx <= 4; dx++) {
			for (int dz = -4; dz <= 4; dz++) {
				if (Math.abs(dx) == 4 || Math.abs(dz) == 4) {
					set(level, origin.offset(dx, 0, dz), Blocks.DARK_OAK_PLANKS.defaultBlockState());
				}
			}
		}
		set(level, origin.offset(3, 1, 3), Blocks.CHEST.defaultBlockState());
		set(level, origin.offset(-3, 1, -3), Blocks.CRAFTING_TABLE.defaultBlockState());
		set(level, origin.offset(0, 1, 0), RobloxifyBlocks.SPAWN_PAD.defaultBlockState());
		set(level, origin.offset(2, 1, -2), RobloxifyBlocks.STUD.defaultBlockState());

		built.spawn = origin.above();
		built.boundsMin = origin.offset(-5, -3, -5);
		built.boundsMax = origin.offset(5, 10, 5);
		built.total = 100;
		built.objective = "Tycoon income 0/100";
		return built;
	}

	// -------------------------------------------------------------- MINIGAME

	private static Built minigame(ServerLevel level, BlockPos origin, Direction facing) {
		Built built = new Built();
		slab(level, origin, 1, RobloxifyBlocks.SPAWN_PAD.defaultBlockState());
		built.spawn = origin.above();
		built.boundsMin = origin.offset(-14, -6, -14);
		built.boundsMax = origin.offset(14, 14, 14);

		BlockState target = RobloxifyBlocks.STUD.defaultBlockState();
		BlockState base = Blocks.SMOOTH_STONE.defaultBlockState();
		int[][] spots = {{-6, -6}, {0, -7}, {6, -6}, {-7, 0}, {7, 0}, {-6, 6}, {0, 7}, {6, 6}};
		for (int[] spot : spots) {
			BlockPos basePos = origin.offset(spot[0], 0, spot[1]);
			set(level, basePos, base);
			set(level, basePos.above(), target);
			built.targets.add(basePos.above().immutable());
		}
		built.total = built.targets.size();
		built.objective = "Targets 0/" + built.total;
		return built;
	}
}

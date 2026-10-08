package com.robloxify.world.block;

import com.robloxify.experience.ExperienceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Vanishes shortly after being stepped on, then comes back. */
public class DisappearingPlatformBlock extends Block {
	public static final int VANISH_DELAY_TICKS = 12;
	public static final int RESTORE_DELAY_TICKS = 60;

	public DisappearingPlatformBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (level instanceof ServerLevel serverLevel) {
			ExperienceManager.scheduleVanish(serverLevel, pos.immutable(), state);
		}
	}
}

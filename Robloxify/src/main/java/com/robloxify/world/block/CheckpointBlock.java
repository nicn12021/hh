package com.robloxify.world.block;

import com.robloxify.experience.ExperienceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** A glowing pad that records the player's progress inside an Experience. */
public class CheckpointBlock extends Block {
	public CheckpointBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (!level.isClientSide() && entity instanceof ServerPlayer player) {
			ExperienceManager.onCheckpoint(player, pos);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(4) != 0) {
			return;
		}
		level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + random.nextDouble(),
				pos.getY() + 1.05, pos.getZ() + random.nextDouble(), 0.0, 0.02, 0.0);
	}
}

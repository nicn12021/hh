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

/** Touching this ends the Experience successfully. */
public class FinishPadBlock extends Block {
	public FinishPadBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (!level.isClientSide() && entity instanceof ServerPlayer player) {
			ExperienceManager.onFinishPad(player, pos);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(),
				pos.getY() + 1.1, pos.getZ() + random.nextDouble(), 0.0, 0.05, 0.0);
	}
}

package com.robloxify.world.block;

import com.robloxify.config.RobloxifyConfig;
import com.robloxify.sound.RobloxifySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Launches whatever lands on it straight up. Classic obby material. */
public class BouncePadBlock extends Block {
	public static final double LAUNCH = 1.15;

	public BouncePadBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (!RobloxifyConfig.get().robloxPhysics) {
			return;
		}
		Vec3 delta = entity.getDeltaMovement();
		if (delta.y > 0.02) {
			return;
		}
		entity.setDeltaMovement(delta.x * 0.6, LAUNCH, delta.z * 0.6);
		entity.hurtMarked = true;
		if (!level.isClientSide()) {
			level.playSound(null, pos, RobloxifySounds.BOUNCE, SoundSource.BLOCKS, 0.9F, 1.0F);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(3) != 0) {
			return;
		}
		level.addParticle(ParticleTypes.CLOUD, pos.getX() + random.nextDouble(),
				pos.getY() + 1.1, pos.getZ() + random.nextDouble(), 0.0, 0.03, 0.0);
	}
}

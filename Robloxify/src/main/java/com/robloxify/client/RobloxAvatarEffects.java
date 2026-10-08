package com.robloxify.client;

import com.robloxify.avatar.AvatarAppearance;
import com.robloxify.avatar.CosmeticCategory;
import com.robloxify.config.RobloxifyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/** Cosmetic auras. Only players who actually own an effect spawn anything. */
public final class RobloxAvatarEffects {
	private static int ticks;

	private RobloxAvatarEffects() {
	}

	public static void tick(Minecraft client) {
		if (client.level == null || client.player == null) {
			return;
		}
		ticks++;
		if (ticks % 4 != 0) {
			return;
		}
		for (AbstractClientPlayer player : client.level.players()) {
			AvatarAppearance appearance = ClientRobloxState.appearanceFor(player.getId());
			if (appearance == null) {
				continue;
			}
			ParticleOptions particle = particleFor(appearance);
			if (particle == null) {
				continue;
			}
			boolean moving = player.getDeltaMovement().horizontalDistanceSqr() > 0.001;
			if (!moving && player != client.player) {
				continue;
			}
			client.level.addParticle(particle, player.getX(), player.getY() + 1.4, player.getZ(),
					0.0, 0.0, 0.0);
		}
	}

	private static ParticleOptions particleFor(AvatarAppearance appearance) {
		if (!RobloxifyConfig.get().robloxAvatar) {
			return null;
		}
		return switch (appearance.cosmetic(CosmeticCategory.EFFECT).shape()) {
			case "sparkle" -> ParticleTypes.END_ROD;
			case "flame" -> ParticleTypes.FLAME;
			case "gold" -> ParticleTypes.CRIT;
			default -> null;
		};
	}
}

package com.robloxify.client;

import com.robloxify.config.RobloxifyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.ParticleTypes;

/** Cheap client side cosmetic particles. Nothing runs unless the player owns the cosmetic. */
public final class RobloxAvatarEffects {
	private static int ticks;

	private RobloxAvatarEffects() {
	}

	public static void tick(Minecraft client) {
		if (client.level == null || client.player == null) {
			return;
		}
		ticks++;
		if (ticks % 4 != 0 || !ClientRobloxState.owns("sparkle_trail")) {
			return;
		}
		for (AbstractClientPlayer player : client.level.players()) {
			if (!ClientRobloxState.isRobloxPlayer(player.getUUID()) || player == client.player) {
				continue;
			}
			if (player.getDeltaMovement().horizontalDistanceSqr() < 0.001) {
				continue;
			}
			client.level.addParticle(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.6, player.getZ(),
					0.0, 0.0, 0.0);
		}
		if (RobloxifyConfig.get().robloxAvatar && ClientRobloxState.isRobloxPlayer(client.player.getUUID())
				&& client.player.getDeltaMovement().horizontalDistanceSqr() > 0.001) {
			client.level.addParticle(ParticleTypes.END_ROD, client.player.getX(), client.player.getY() + 0.6,
					client.player.getZ(), 0.0, 0.0, 0.0);
		}
	}
}

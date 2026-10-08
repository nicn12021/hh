package com.robloxify.easteregg;

import com.robloxify.Robloxify;
import com.robloxify.badge.Badges;
import com.robloxify.config.RobloxifyConfig;
import com.robloxify.server.RobloxifyService;
import com.robloxify.world.RobloxifyBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The secret stuff: a suspicious NPC, a hidden Roblox Experience room and the infamous
 * Juaninho The Myth badge.
 */
public final class EasterEggs {
	public static final String NPC_NAME = "Juaninho The Myth";
	private static final int AREA_RADIUS = 8;
	private static final Map<UUID, BlockPos> SECRET_AREAS = new HashMap<>();

	private EasterEggs() {
	}

	/** Spawns the extremely suspicious NPC next to the command source. */
	public static int spawnSuspiciousNpc(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		ServerLevel level = player.level();
		BlockPos pos = player.blockPosition().relative(player.getDirection(), 2);
		Mannequin npc = EntityTypes.MANNEQUIN.spawn(level, pos, EntitySpawnReason.COMMAND);
		if (npc == null) {
			source.sendFailure(Component.literal("Could not spawn the suspicious NPC here."));
			return 0;
		}
		npc.setCustomName(Component.literal(NPC_NAME).withStyle(ChatFormatting.DARK_PURPLE));
		npc.setCustomNameVisible(true);
		npc.setInvulnerable(true);
		npc.setNoGravity(false);
		source.sendSuccess(() -> Component.literal("[Robloxify] Something suspicious appeared...")
				.withStyle(ChatFormatting.DARK_PURPLE), false);
		return 1;
	}

	/** Builds a small hidden room made of studs - the secret Roblox Experience. */
	public static int buildSecretArea(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		ServerLevel level = player.level();
		BlockPos origin = player.blockPosition().below(6);
		BlockState stud = RobloxifyBlocks.STUD.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();

		for (int x = -3; x <= 3; x++) {
			for (int z = -3; z <= 3; z++) {
				level.setBlockAndUpdate(origin.offset(x, 0, z), stud);
				level.setBlockAndUpdate(origin.offset(x, 1, z), air);
				level.setBlockAndUpdate(origin.offset(x, 2, z), air);
				level.setBlockAndUpdate(origin.offset(x, 3, z), air);
				if (Math.abs(x) == 3 || Math.abs(z) == 3) {
					for (int y = 1; y <= 3; y++) {
						level.setBlockAndUpdate(origin.offset(x, y, z), stud);
					}
				}
			}
		}
		level.setBlockAndUpdate(origin.offset(0, 3, 0), Blocks.GLOWSTONE.defaultBlockState());
		level.sendParticles(ParticleTypes.END_ROD, origin.getX() + 0.5, origin.getY() + 2.0,
				origin.getZ() + 0.5, 30, 1.5, 1.0, 1.5, 0.05);
		SECRET_AREAS.put(player.getUUID(), origin.immutable());
		source.sendSuccess(() -> Component.literal("[Robloxify] A secret Roblox Experience was built below you.")
				.withStyle(ChatFormatting.LIGHT_PURPLE), false);
		return 1;
	}

	/** Called when a player right-clicks an entity. */
	public static void onInteract(ServerPlayer player, Entity entity) {
		if (!RobloxifyConfig.get().easterEggs || !(entity instanceof Mannequin stand)) {
			return;
		}
		if (stand.getCustomName() == null || !NPC_NAME.equals(stand.getCustomName().getString())) {
			return;
		}
		boolean wearingAvatar = RobloxifyService.profile(player).robloxAvatar;
		boolean inTheEnd = player.level().dimension() == Level.END;
		boolean holdingStud = player.getMainHandItem().is(RobloxifyBlocks.STUD.asItem())
				|| player.getOffhandItem().is(RobloxifyBlocks.STUD.asItem());

		if (wearingAvatar && inTheEnd && holdingStud) {
			RobloxifyService.grantBadge(player, Badges.JUANINHO_THE_MYTH);
			player.sendSystemMessage(Component.literal("Bro thought he cooked.").withStyle(ChatFormatting.DARK_PURPLE));
			return;
		}
		player.sendSystemMessage(Component.literal("Juaninho stares at you. He knows you are not ready.")
				.withStyle(ChatFormatting.DARK_GRAY));
		RobloxifyService.play(player, com.robloxify.sound.RobloxifySounds.OOF, 0.6F);
	}

	/** Low frequency tick hook (every 200 ticks) for the hidden areas. */
	public static void tick(MinecraftServer server) {
		if (SECRET_AREAS.isEmpty() || !RobloxifyConfig.get().easterEggs) {
			return;
		}
		SECRET_AREAS.entrySet().removeIf(entry -> {
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			if (player == null) {
				return true;
			}
			BlockPos pos = entry.getValue();
			if (player.blockPosition().distSqr(pos) <= AREA_RADIUS * AREA_RADIUS) {
				ServerLevel level = player.level();
				level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5,
						3, 1.5, 1.0, 1.5, 0.0);
			}
			return false;
		});
	}
}

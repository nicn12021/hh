package com.robloxify.event;

import com.robloxify.badge.Badges;
import com.robloxify.config.RobloxifyConfig;
import com.robloxify.data.Profile;
import com.robloxify.data.RobloxifyData;
import com.robloxify.easteregg.EasterEggs;
import com.robloxify.experience.ObbyManager;
import com.robloxify.net.RobloxifyNetworking;
import com.robloxify.net.RobloxifyPayloads;
import com.robloxify.server.RobloxifyService;
import com.robloxify.sound.RobloxifySounds;
import com.robloxify.world.RobloxifyBlocks;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Wires Robloxify into the vanilla game through Fabric's event API. */
public final class RobloxifyEvents {
	private static int ticks;

	private RobloxifyEvents() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTING.register(RobloxifyData::init);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> RobloxifyData.shutdown());

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoin(handler.getPlayer(), server));

		ServerTickEvents.END_SERVER_TICK.register(RobloxifyEvents::onEndTick);

		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
				return;
			}
			onBlockMined(serverPlayer);
		});

		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.PASS;
			}
			onBlockPlaced(serverPlayer, player.getItemInHand(hand), hitResult.getBlockPos());
			return InteractionResult.PASS;
		});

		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
				EasterEggs.onInteract(serverPlayer, entity);
			}
			return InteractionResult.PASS;
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (!RobloxifyConfig.get().easterEggs || !(entity instanceof ServerPlayer player)) {
				return;
			}
			player.level().playSound(null, player.blockPosition(), RobloxifySounds.OOF, SoundSource.PLAYERS, 1.0F, 1.0F);
			player.sendSystemMessage(Component.literal("oof").withStyle(ChatFormatting.DARK_GRAY));
		});
	}

	private static void onJoin(ServerPlayer player, MinecraftServer server) {
		RobloxifyService.syncProfile(player);
		for (ServerPlayer other : server.getPlayerList().getPlayers()) {
			RobloxifyNetworking.sendTo(player, new RobloxifyPayloads.Avatar(other.getUUID(),
					RobloxifyService.profile(other).robloxAvatar));
		}
		RobloxifyNetworking.broadcast(server, new RobloxifyPayloads.Avatar(player.getUUID(),
				RobloxifyService.profile(player).robloxAvatar));
	}

	private static void onBlockMined(ServerPlayer player) {
		Profile profile = RobloxifyService.profile(player);
		profile.blocksMined++;
		RobloxifyData.get().markDirty();
		if (profile.blocksMined == 1) {
			RobloxifyService.grantBadge(player, Badges.FIRST_BLOCK);
		} else if (profile.blocksMined == 100) {
			RobloxifyService.grantBadge(player, Badges.MINING_EXPERIENCE);
		}
	}

	private static void onBlockPlaced(ServerPlayer player, ItemStack stack, BlockPos pos) {
		if (!(stack.getItem() instanceof BlockItem)) {
			return;
		}
		Profile profile = RobloxifyService.profile(player);
		profile.blocksPlaced++;
		RobloxifyData.get().markDirty();

		if (stack.is(RobloxifyBlocks.STUD.asItem()) && RobloxifyConfig.get().easterEggs) {
			ServerLevel level = player.level();
			level.playSound(null, pos, RobloxifySounds.UI_OPEN, SoundSource.BLOCKS, 0.6F, 1.6F);
			if (profile.owned.contains("gold_studs")) {
				level.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
						12, 0.4, 0.4, 0.4, 0.05);
			}
			if (profile.blocksPlaced % 10 == 0) {
				RobloxifyService.addRobux(player, 1);
			}
		}

		if (profile.blocksPlaced == 500) {
			RobloxifyService.grantBadge(player, Badges.BUILDER);
		}
	}

	private static void onEndTick(MinecraftServer server) {
		ObbyManager.tick(server);
		ticks++;
		if (ticks % 20 == 0) {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				RobloxifyService.checkEnderRobloxian(player);
			}
		}
		if (ticks % 200 == 0) {
			EasterEggs.tick(server);
			RobloxifyData data = RobloxifyData.get();
			if (data != null) {
				data.flushIfDirty();
			}
		}
	}
}

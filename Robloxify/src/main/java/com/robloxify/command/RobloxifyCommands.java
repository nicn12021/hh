package com.robloxify.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.robloxify.badge.Badge;
import com.robloxify.badge.Badges;
import com.robloxify.config.RobloxifyConfig;
import com.robloxify.data.Profile;
import com.robloxify.easteregg.EasterEggs;
import com.robloxify.experience.ObbyManager;
import com.robloxify.server.RobloxifyService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/** All {@code /robloxify} subcommands. */
public final class RobloxifyCommands {
	private RobloxifyCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal("robloxify")
						.executes(context -> info(context.getSource()))
						.then(Commands.literal("info").executes(context -> info(context.getSource())))
						.then(Commands.literal("robux")
								.then(Commands.literal("add")
										.then(Commands.argument("amount", IntegerArgumentType.integer(-1000000, 1000000))
												.executes(context -> {
													ServerPlayer player = context.getSource().getPlayerOrException();
													RobloxifyService.addRobux(player, IntegerArgumentType.getInteger(context, "amount"));
													return 1;
												})))
								.then(Commands.literal("set")
										.then(Commands.argument("amount", IntegerArgumentType.integer(0, 100000000))
												.executes(context -> {
													ServerPlayer player = context.getSource().getPlayerOrException();
													RobloxifyService.setRobux(player, IntegerArgumentType.getInteger(context, "amount"));
													return 1;
												})))
								.then(Commands.literal("get").executes(context -> {
									ServerPlayer player = context.getSource().getPlayerOrException();
									Profile profile = RobloxifyService.profile(player);
									context.getSource().sendSuccess(() -> Component.literal("[Robloxify] Robux: " + profile.robux)
											.withStyle(ChatFormatting.YELLOW), false);
									return profile.robux;
								})))
						.then(Commands.literal("avatar")
								.executes(context -> toggleAvatar(context.getSource()))
								.then(Commands.literal("on").executes(context -> setAvatar(context.getSource(), true)))
								.then(Commands.literal("off").executes(context -> setAvatar(context.getSource(), false)))
								.then(Commands.literal("toggle").executes(context -> toggleAvatar(context.getSource()))))
						.then(Commands.literal("badge")
								.then(Commands.literal("list").executes(context -> listBadges(context.getSource())))
								.then(Commands.literal("grant")
										.then(Commands.argument("id", StringArgumentType.word())
												.executes(context -> {
													ServerPlayer player = context.getSource().getPlayerOrException();
													String id = StringArgumentType.getString(context, "id");
													Badge badge = Badges.byId(id);
													if (badge == null) {
														context.getSource().sendFailure(Component.literal("Unknown badge: " + id));
														return 0;
													}
													RobloxifyService.grantBadge(player, badge);
													return 1;
												}))))
						.then(Commands.literal("emote")
								.then(Commands.argument("name", StringArgumentType.word())
										.executes(context -> {
											ServerPlayer player = context.getSource().getPlayerOrException();
											RobloxifyService.emote(player, StringArgumentType.getString(context, "name"));
											return 1;
										})))
						.then(Commands.literal("obby")
								.then(Commands.literal("start").executes(context -> {
									ObbyManager.start(context.getSource().getPlayerOrException());
									return 1;
								}))
								.then(Commands.literal("stop").executes(context -> {
									ObbyManager.stop(context.getSource().getPlayerOrException(), false);
									return 1;
								})))
						.then(Commands.literal("config")
								.then(Commands.literal("list").executes(context -> listConfig(context.getSource())))
								.then(Commands.argument("key", StringArgumentType.word())
										.then(Commands.argument("value", StringArgumentType.word())
												.executes(context -> setConfig(context.getSource(),
														StringArgumentType.getString(context, "key"),
														StringArgumentType.getString(context, "value"))))))
						.then(Commands.literal("easteregg")
								.then(Commands.literal("npc").executes(context -> EasterEggs.spawnSuspiciousNpc(context.getSource())))
								.then(Commands.literal("area").executes(context -> EasterEggs.buildSecretArea(context.getSource()))))));
	}

	private static int info(CommandSourceStack source) {
		source.sendSuccess(() -> Component.literal("[Robloxify] ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal("v" + com.robloxify.Robloxify.VERSION).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" - /robloxify robux|avatar|badge|emote|obby|config|easteregg")
						.withStyle(ChatFormatting.WHITE)), false);
		return 1;
	}

	private static int toggleAvatar(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		RobloxifyService.setAvatar(player, !RobloxifyService.profile(player).robloxAvatar);
		return 1;
	}

	private static int setAvatar(CommandSourceStack source, boolean value) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		RobloxifyService.setAvatar(source.getPlayerOrException(), value);
		return 1;
	}

	private static int listBadges(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		Profile profile = RobloxifyService.profile(source.getPlayerOrException());
		for (Badge badge : Badges.ALL) {
			boolean owned = profile.hasBadge(badge.id());
			if (badge.secret() && !owned) {
				continue;
			}
			source.sendSuccess(() -> Component.literal((owned ? "[x] " : "[ ] ") + badge.icon() + " " + badge.name()
					+ " - " + badge.description()).withStyle(owned ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY), false);
		}
		return 1;
	}

	private static int listConfig(CommandSourceStack source) {
		for (Map.Entry<String, Boolean> entry : RobloxifyConfig.get().toggles().entrySet()) {
			source.sendSuccess(() -> Component.literal(entry.getKey() + " = " + entry.getValue())
				.withStyle(entry.getValue() ? ChatFormatting.GREEN : ChatFormatting.RED), false);
		}
		return 1;
	}

	private static int setConfig(CommandSourceStack source, String key, String value) {
		boolean enabled = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on") || value.equals("1");
		if (RobloxifyConfig.get().setToggle(key, enabled)) {
			source.sendSuccess(() -> Component.literal("[Robloxify] " + key + " = " + enabled)
					.withStyle(ChatFormatting.YELLOW), false);
			return 1;
		}
		source.sendFailure(Component.literal("Unknown config key: " + key));
		return 0;
	}
}

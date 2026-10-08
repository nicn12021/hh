package com.robloxify.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.robloxify.Robloxify;
import com.robloxify.avatar.Cosmetic;
import com.robloxify.avatar.CosmeticCatalog;
import com.robloxify.avatar.CosmeticCategory;
import com.robloxify.badge.Badge;
import com.robloxify.badge.Badges;
import com.robloxify.config.RobloxifyConfig;
import com.robloxify.data.Profile;
import com.robloxify.easteregg.EasterEggs;
import com.robloxify.experience.ExperienceManager;
import com.robloxify.experience.ExperienceType;
import com.robloxify.server.RobloxifyService;
import com.robloxify.util.Robux;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Map;

/** All {@code /robloxify} subcommands. */
public final class RobloxifyCommands {
	/** 26.2 replaced numeric op levels with permission sets. Gamemaster == the old level 2. */
	private static final java.util.function.Predicate<CommandSourceStack> OP =
			source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);

	private RobloxifyCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("robloxify")
				.executes(context -> help(context.getSource()))
				.then(Commands.literal("help").executes(context -> help(context.getSource())))
				.then(Commands.literal("reload")
						.requires(OP)
						.executes(context -> reload(context.getSource())))
				.then(Commands.literal("robux")
						.then(Commands.literal("get").executes(context -> robuxGet(context.getSource())))
						.then(Commands.literal("add")
								.requires(OP)
								.then(Commands.argument("amount", IntegerArgumentType.integer(-1000000, 1000000))
										.executes(context -> {
											RobloxifyService.addRobux(context.getSource().getPlayerOrException(),
													IntegerArgumentType.getInteger(context, "amount"));
											return 1;
										})))
						.then(Commands.literal("set")
								.requires(OP)
								.then(Commands.argument("amount", IntegerArgumentType.integer(0, 100000000))
										.executes(context -> {
											RobloxifyService.setRobux(context.getSource().getPlayerOrException(),
													IntegerArgumentType.getInteger(context, "amount"));
											return 1;
										}))))
				.then(Commands.literal("avatar")
						.executes(context -> toggleAvatar(context.getSource()))
						.then(Commands.literal("on").executes(context -> setAvatar(context.getSource(), true)))
						.then(Commands.literal("off").executes(context -> setAvatar(context.getSource(), false)))
						.then(Commands.literal("toggle").executes(context -> toggleAvatar(context.getSource()))))
				.then(Commands.literal("cosmetics")
						.executes(context -> listCosmetics(context.getSource()))
						.then(Commands.literal("equip")
								.then(Commands.argument("id", StringArgumentType.word())
										.executes(context -> {
											RobloxifyService.equip(context.getSource().getPlayerOrException(),
													StringArgumentType.getString(context, "id"));
											return 1;
										})))
						.then(Commands.literal("buy")
								.requires(OP)
								.then(Commands.argument("id", StringArgumentType.word())
										.executes(context -> {
											RobloxifyService.buy(context.getSource().getPlayerOrException(),
													StringArgumentType.getString(context, "id"));
											return 1;
										}))))
				.then(Commands.literal("badge")
						.then(Commands.literal("list").executes(context -> listBadges(context.getSource())))
						.then(Commands.literal("grant")
								.requires(OP)
								.then(Commands.argument("id", StringArgumentType.word())
										.executes(context -> grantBadge(context.getSource(),
												StringArgumentType.getString(context, "id"))))))
				.then(Commands.literal("emote")
						.then(Commands.argument("name", StringArgumentType.word())
								.executes(context -> {
									RobloxifyService.emote(context.getSource().getPlayerOrException(),
											StringArgumentType.getString(context, "name"));
									return 1;
								})))
				.then(Commands.literal("experience")
						.executes(context -> listExperiences(context.getSource()))
						.then(Commands.literal("list").executes(context -> listExperiences(context.getSource())))
						.then(Commands.literal("start")
								.then(Commands.argument("id", StringArgumentType.word())
										.executes(context -> {
											RobloxifyService.startExperience(context.getSource().getPlayerOrException(),
													StringArgumentType.getString(context, "id"));
											return 1;
										})))
						.then(Commands.literal("stop").executes(context -> {
							ExperienceManager.stop(context.getSource().getPlayerOrException(), false);
							return 1;
						})))
				.then(Commands.literal("config")
						.then(Commands.literal("list").executes(context -> listConfig(context.getSource())))
						.then(Commands.argument("key", StringArgumentType.word())
								.then(Commands.argument("value", StringArgumentType.word())
										.requires(OP)
										.executes(context -> setConfig(context.getSource(),
												StringArgumentType.getString(context, "key"),
												StringArgumentType.getString(context, "value"))))))
				.then(Commands.literal("easteregg")
						.requires(OP)
						.then(Commands.literal("npc").executes(context -> EasterEggs.spawnSuspiciousNpc(context.getSource())))
						.then(Commands.literal("area").executes(context -> EasterEggs.buildSecretArea(context.getSource()))));

			dispatcher.register(root);
		});
	}

	private static int help(CommandSourceStack source) {
		String[] lines = {
			"/robloxify avatar <on|off|toggle>       - switch look",
			"/robloxify robux get                    - your balance",
			"/robloxify cosmetics                    - avatar catalogue",
			"/robloxify cosmetics equip <id>         - equip an owned item",
			"/robloxify badge list                   - badge collection",
			"/robloxify emote <wave|dance|point|...>  - play an emote",
			"/robloxify experience                   - list Experiences",
			"/robloxify experience start <id>        - play an Experience",
			"/robloxify experience stop              - leave an Experience",
			"/robloxify config list                  - feature toggles",
			"/robloxify reload                       - reload config (op)",
		};
		source.sendSuccess(() -> Component.literal("Robloxify v" + Robloxify.VERSION)
				.withStyle(ChatFormatting.GOLD), false);
		for (String line : lines) {
			source.sendSuccess(() -> Component.literal(line).withStyle(ChatFormatting.GRAY), false);
		}
		return 1;
	}

	private static int reload(CommandSourceStack source) {
		RobloxifyConfig.load();
		source.sendSuccess(() -> Component.literal("[Robloxify] Config reloaded.")
				.withStyle(ChatFormatting.YELLOW), false);
		return 1;
	}

	private static int robuxGet(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		Profile profile = RobloxifyService.profile(source.getPlayerOrException());
		source.sendSuccess(() -> Component.literal("[Robloxify] Balance: " + Robux.format(profile.robux))
				.withStyle(ChatFormatting.GOLD), false);
		return profile.robux;
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

	private static int listCosmetics(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		Profile profile = RobloxifyService.profile(source.getPlayerOrException());
		for (CosmeticCategory category : CosmeticCategory.values()) {
			source.sendSuccess(() -> Component.literal("-- " + category.label() + " --")
					.withStyle(ChatFormatting.AQUA), false);
			for (Cosmetic cosmetic : CosmeticCatalog.of(category)) {
				boolean owned = profile.owns(cosmetic.id());
				boolean equipped = cosmetic.id().equals(profile.appearance.slot(category));
				String prefix = equipped ? "[x] " : owned ? "[ ] " : "[R$] ";
				source.sendSuccess(() -> Component.literal(prefix + cosmetic.id() + " - " + cosmetic.name()
							+ (cosmetic.isFree() ? "" : " (" + Robux.format(cosmetic.price()) + ")"))
						.withStyle(equipped ? ChatFormatting.GREEN : owned ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY), false);
			}
		}
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

	private static int grantBadge(CommandSourceStack source, String id) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		Badge badge = Badges.byId(id);
		if (badge == null) {
			source.sendFailure(Component.literal("Unknown badge: " + id));
			return 0;
		}
		RobloxifyService.grantBadge(source.getPlayerOrException(), badge);
		return 1;
	}

	private static int listExperiences(CommandSourceStack source) {
		for (ExperienceType type : ExperienceType.values()) {
			source.sendSuccess(() -> Component.literal(type.id() + " - " + type.displayName()
					+ " [" + type.tag() + "]  +" + Robux.format(type.reward()))
					.withStyle(ChatFormatting.YELLOW), false);
			source.sendSuccess(() -> Component.literal("    " + type.description())
					.withStyle(ChatFormatting.GRAY), false);
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

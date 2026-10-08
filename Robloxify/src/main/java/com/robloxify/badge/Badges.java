package com.robloxify.badge;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/** The built-in badge catalogue. Icons are unicode glyphs drawn with the game font. */
public final class Badges {
	public static final Badge WELCOME = new Badge("welcome", "Welcome!",
			"Join a world with Robloxify installed.", "\u2605", 25);
	public static final Badge FIRST_BLOCK = new Badge("first_block", "First Block",
			"Break your very first block.", "\u25A0", 10);
	public static final Badge MINING_EXPERIENCE = new Badge("mining_experience", "Mining Experience",
			"Mine 100 blocks.", "\u26CF", 75);
	public static final Badge ROBLOXIAN = new Badge("robloxian", "Robloxian",
			"Activate the Roblox avatar.", "\u25C6", 50);
	public static final Badge BUILDER = new Badge("builder", "Builder",
			"Place 500 blocks.", "\u25A3", 150);
	public static final Badge OBBY_BEGINNER = new Badge("obby_beginner", "Obby Beginner",
			"Start your first obby.", "\u2691", 25);
	public static final Badge OBBY_SURVIVOR = new Badge("obby_survivor", "Obby Survivor",
			"Complete an obby.", "\u2691", 250);
	public static final Badge SPEEDRUNNER = new Badge("speedrunner", "Speedrunner",
			"Finish an obby in under 60 seconds.", "\u26A1", 400);
	public static final Badge FIRST_EXPERIENCE = new Badge("first_experience", "First Experience",
			"Complete any Experience.", "\u25B6", 100);
	public static final Badge EXPLORER = new Badge("explorer", "Explorer",
			"Complete three different Experiences.", "\u25CE", 350);
	public static final Badge AVATAR_COLLECTOR = new Badge("avatar_collector", "Avatar Collector",
			"Own eight avatar items.", "\u25C8", 200);
	public static final Badge MILLIONAIRE = new Badge("millionaire", "Millionaire",
			"Hold 10,000 Robux at once.", "\u25C9", 500);
	public static final Badge ENDER_ROBLOXIAN = new Badge("ender_robloxian", "Ender Robloxian",
			"Enter the End while wearing the Roblox avatar.", "\u25CE", 300);
	public static final Badge JUANINHO_THE_MYTH = new Badge("juaninho_the_myth", "Juaninho The Myth",
			"Bro thought he cooked.", "\u2620", 999, true);

	public static final List<Badge> ALL = List.of(
			WELCOME, FIRST_BLOCK, MINING_EXPERIENCE, ROBLOXIAN, BUILDER,
			OBBY_BEGINNER, OBBY_SURVIVOR, SPEEDRUNNER, FIRST_EXPERIENCE, EXPLORER,
			AVATAR_COLLECTOR, MILLIONAIRE, ENDER_ROBLOXIAN, JUANINHO_THE_MYTH);

	private Badges() {
	}

	@Nullable
	public static Badge byId(String id) {
		for (Badge badge : ALL) {
			if (badge.id().equals(id)) {
				return badge;
			}
		}
		return null;
	}
}

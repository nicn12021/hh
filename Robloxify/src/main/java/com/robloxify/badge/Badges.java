package com.robloxify.badge;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/** The built-in badge catalogue. */
public final class Badges {
	public static final Badge FIRST_BLOCK = new Badge("first_block", "First Block",
			"Break your very first block.", "\u25A0", 10);
	public static final Badge MINING_EXPERIENCE = new Badge("mining_experience", "Mining Experience",
			"Mine 100 blocks.", "\u26CF", 75);
	public static final Badge ROBLOXIAN = new Badge("robloxian", "Robloxian",
			"Activate the Roblox avatar.", "\u25C6", 50);
	public static final Badge BUILDER = new Badge("builder", "Builder",
			"Place 500 blocks.", "\u25A3", 150);
	public static final Badge OBBY_SURVIVOR = new Badge("obby_survivor", "Obby Survivor",
			"Complete an obby.", "\u2691", 250);
	public static final Badge ENDER_ROBLOXIAN = new Badge("ender_robloxian", "Ender Robloxian",
			"Enter the End while wearing the Roblox avatar.", "\u25CE", 300);
	public static final Badge JUANINHO_THE_MYTH = new Badge("juaninho_the_myth", "Juaninho The Myth",
			"Bro thought he cooked.", "\u2620", 999, true);

	public static final List<Badge> ALL = List.of(
			FIRST_BLOCK, MINING_EXPERIENCE, ROBLOXIAN, BUILDER,
			OBBY_SURVIVOR, ENDER_ROBLOXIAN, JUANINHO_THE_MYTH);

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

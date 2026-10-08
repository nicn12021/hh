package com.robloxify.experience;

import org.jetbrains.annotations.Nullable;

/** The Robloxify Experience catalogue. */
public enum ExperienceType {
	OBBY("obby", "Obby", "Platforming",
			"Jump the platforms, bounce off the pads, reach the finish.", 250, 0xFF00B06A),
	SWORD_ARENA("sword_arena", "Sword Arena", "Combat",
			"Clear every wave of mobs with your sword.", 300, 0xFFC0392B),
	RACE("race", "Speed Race", "Racing",
			"Hit every checkpoint before the clock runs out.", 300, 0xFF3498DB),
	TYCOON("tycoon", "Tycoon", "Progression",
			"Stand on your plot and collect passive Robux.", 350, 0xFFF1C40F),
	SURVIVAL("survival", "Survival", "Waves",
			"Survive escalating waves of enemies.", 400, 0xFF8E44AD),
	MINIGAME("minigame", "Minigame", "Challenge",
			"Break every glowing target before the clock runs out.", 200, 0xFFE67E22);

	private final String id;
	private final String name;
	private final String tag;
	private final String description;
	private final int reward;
	private final int accent;

	ExperienceType(String id, String name, String tag, String description, int reward, int accent) {
		this.id = id;
		this.name = name;
		this.tag = tag;
		this.description = description;
		this.reward = reward;
		this.accent = accent;
	}

	public String id() {
		return id;
	}

	public String displayName() {
		return name;
	}

	public String tag() {
		return tag;
	}

	public String description() {
		return description;
	}

	public int reward() {
		return reward;
	}

	public int accent() {
		return accent;
	}

	@Nullable
	public static ExperienceType byId(String id) {
		for (ExperienceType type : values()) {
			if (type.id.equals(id)) {
				return type;
			}
		}
		return null;
	}
}

package com.robloxify.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** The emote catalogue. Durations are in ticks. */
public final class Emotes {
	public static final Map<String, Integer> DURATIONS = new LinkedHashMap<>();

	static {
		DURATIONS.put("wave", 30);
		DURATIONS.put("dance", 80);
		DURATIONS.put("point", 30);
		DURATIONS.put("idle", 40);
		DURATIONS.put("walk", 40);
		DURATIONS.put("run", 40);
		DURATIONS.put("jump", 20);
		DURATIONS.put("fall", 20);
	}

	private Emotes() {
	}

	public static boolean isValid(String name) {
		return name != null && DURATIONS.containsKey(name.toLowerCase(Locale.ROOT));
	}

	public static String normalise(String name) {
		return name == null ? "" : name.toLowerCase(Locale.ROOT);
	}

	public static int duration(String name) {
		return DURATIONS.getOrDefault(normalise(name), 20);
	}
}

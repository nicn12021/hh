package com.robloxify.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.robloxify.Robloxify;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight JSON configuration. No external dependencies beyond Gson, which is already
 * shipped with Minecraft.
 */
public final class RobloxifyConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static RobloxifyConfig instance;

	// ---- Feature toggles -------------------------------------------------
	public boolean robloxAvatar = true;
	public boolean robloxHud = true;
	public boolean robux = true;
	public boolean badges = true;
	public boolean experiences = true;
	public boolean robloxSounds = true;
	public boolean emotes = true;
	public boolean robloxUi = true;
	public boolean easterEggs = true;

	// ---- Values ----------------------------------------------------------
	public int startingRobux = 1250;
	public int hudOffsetX = 4;
	public int hudOffsetY = 4;

	public static RobloxifyConfig get() {
		if (instance == null) {
			load();
		}
		return instance;
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(Robloxify.MOD_ID + ".json");
	}

	public static synchronized void load() {
		Path path = configPath();
		RobloxifyConfig loaded = null;
		try {
			if (Files.exists(path)) {
				loaded = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), RobloxifyConfig.class);
			}
		} catch (Exception e) {
			Robloxify.LOGGER.warn("Could not read config, using defaults", e);
		}
		instance = loaded != null ? loaded : new RobloxifyConfig();
		instance.save();
	}

	public synchronized void save() {
		Path path = configPath();
		try {
			Path parent = path.toAbsolutePath().getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(path, GSON.toJson(this), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Robloxify.LOGGER.warn("Could not write config", e);
		}
	}

	/** Human readable map of toggle -> current value, used by the settings screen and command. */
	public Map<String, Boolean> toggles() {
		Map<String, Boolean> map = new LinkedHashMap<>();
		map.put("roblox_avatar", robloxAvatar);
		map.put("roblox_hud", robloxHud);
		map.put("robux", robux);
		map.put("badges", badges);
		map.put("experiences", experiences);
		map.put("roblox_sounds", robloxSounds);
		map.put("emotes", emotes);
		map.put("roblox_ui", robloxUi);
		map.put("easter_eggs", easterEggs);
		return map;
	}

	public boolean setToggle(String key, boolean value) {
		switch (key) {
			case "roblox_avatar" -> robloxAvatar = value;
			case "roblox_hud" -> robloxHud = value;
			case "robux" -> robux = value;
			case "badges" -> badges = value;
			case "experiences" -> experiences = value;
			case "roblox_sounds" -> robloxSounds = value;
			case "emotes" -> emotes = value;
			case "roblox_ui" -> robloxUi = value;
			case "easter_eggs" -> easterEggs = value;
			default -> {
				return false;
			}
		}
		save();
		return true;
	}
}

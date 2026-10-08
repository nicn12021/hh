package com.robloxify.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.robloxify.Robloxify;
import com.robloxify.config.RobloxifyConfig;
import net.minecraft.server.MinecraftServer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side authoritative store. Written to {@code <server dir>/robloxify_data.json}.
 * Only touched when something actually changes, so it costs nothing during normal play.
 */
public final class RobloxifyData {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static RobloxifyData instance;

	private final Path file;
	private Map<String, Profile> players = new HashMap<>();
	private boolean dirty;

	private RobloxifyData(Path file) {
		this.file = file;
		read();
	}

	public static synchronized void init(MinecraftServer server) {
		if (instance == null) {
			instance = new RobloxifyData(server.getServerDirectory().resolve(Robloxify.MOD_ID + "_data.json"));
		}
	}

	public static RobloxifyData get() {
		return instance;
	}

	public static synchronized void shutdown() {
		if (instance != null) {
			instance.write();
			instance = null;
		}
	}

	public Profile profile(UUID uuid) {
		Profile profile = players.computeIfAbsent(uuid.toString(), key -> new Profile());
		if (profile.robux == Profile.UNINITIALISED) {
			profile.robux = Math.max(0, RobloxifyConfig.get().startingRobux);
			profile.grantStartingItems();
			dirty = true;
		}
		if (profile.appearance == null) {
			profile.appearance = new com.robloxify.avatar.AvatarAppearance();
		}
		return profile;
	}

	public void markDirty() {
		dirty = true;
	}

	/** Called on a low frequency tick hook; only writes when something changed. */
	public void flushIfDirty() {
		if (dirty) {
			write();
		}
	}

	private void read() {
		try {
			if (Files.exists(file)) {
				DataFile data = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), DataFile.class);
				if (data != null && data.players != null) {
					players = data.players;
				}
			}
		} catch (Exception e) {
			Robloxify.LOGGER.warn("Could not read Robloxify data, starting fresh", e);
		}
	}

	public synchronized void write() {
		try {
			Path parent = file.toAbsolutePath().getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			DataFile data = new DataFile();
			data.players = players;
			Files.writeString(file, GSON.toJson(data), StandardCharsets.UTF_8);
			dirty = false;
		} catch (Exception e) {
			Robloxify.LOGGER.warn("Could not write Robloxify data", e);
		}
	}

	private static final class DataFile {
		Map<String, Profile> players;
	}
}

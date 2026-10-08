package com.robloxify.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Per-player persistent state. Plain fields so Gson can serialise it directly. */
public final class Profile {
	public static final int UNINITIALISED = -1;

	public int robux = UNINITIALISED;
	public int blocksMined;
	public int blocksPlaced;
	public boolean robloxAvatar;
	public int obbiesCompleted;
	public long obbyBestMillis;
	public boolean metJuaninho;
	public Map<String, Long> badges = new LinkedHashMap<>();
	public List<String> owned = new ArrayList<>();

	public boolean hasBadge(String id) {
		return badges.containsKey(id);
	}

	public boolean grantBadge(String id, long timestamp) {
		return badges.putIfAbsent(id, timestamp) == null;
	}
}

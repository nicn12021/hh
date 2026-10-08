package com.robloxify.data;

import com.robloxify.avatar.AvatarAppearance;
import com.robloxify.avatar.CosmeticCatalog;

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
	public int experiencesPlayed;
	public long obbyBestMillis;
	public boolean metJuaninho;
	public Map<String, Long> badges = new LinkedHashMap<>();
	public List<String> owned = new ArrayList<>();
	public List<String> completedExperiences = new ArrayList<>();
	public AvatarAppearance appearance = new AvatarAppearance();

	public boolean hasBadge(String id) {
		return badges.containsKey(id);
	}

	public boolean grantBadge(String id, long timestamp) {
		return badges.putIfAbsent(id, timestamp) == null;
	}

	public boolean owns(String cosmeticId) {
		return owned.contains(cosmeticId);
	}

	/** Free items are always owned; this also migrates older saves. */
	public void grantStartingItems() {
		for (String id : CosmeticCatalog.startingItems()) {
			if (!owned.contains(id)) {
				owned.add(id);
			}
		}
	}
}

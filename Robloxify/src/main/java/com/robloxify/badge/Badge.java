package com.robloxify.badge;

/**
 * A Robloxify badge definition.
 *
 * @param id          stable identifier used for persistence and networking
 * @param name        display name
 * @param description display description
 * @param icon        a single unicode glyph used as the icon (no proprietary assets)
 * @param reward      robux granted on unlock
 * @param secret      secret badges are not listed until unlocked
 */
public record Badge(String id, String name, String description, String icon, int reward, boolean secret) {
	public Badge(String id, String name, String description, String icon, int reward) {
		this(id, name, description, icon, reward, false);
	}
}

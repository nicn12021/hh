package com.robloxify.client.notification;

/** One Roblox style notification card. */
public final class RobloxNotification {
	public final String kind;
	public final String title;
	public final String subtitle;
	public final int robux;
	public int age;
	public int lifetime = 140;

	public RobloxNotification(String kind, String title, String subtitle, int robux) {
		this.kind = kind;
		this.title = title;
		this.subtitle = subtitle;
		this.robux = robux;
	}

	public int accent() {
		return switch (kind) {
			case "badge" -> 0xFFFFC64A;
			case "checkpoint" -> 0xFF00E08A;
			case "complete" -> 0xFF7CFC00;
			case "purchase" -> 0xFF4AA8FF;
			case "experience" -> 0xFFB36BFF;
			case "avatar" -> 0xFF00D4FF;
			case "robux" -> 0xFFFFC64A;
			case "fail" -> 0xFFFF5B5B;
			default -> 0xFF9AA0A6;
		};
	}

	public String icon() {
		return switch (kind) {
			case "badge" -> "\u2605";
			case "checkpoint" -> "\u2691";
			case "complete" -> "\u2714";
			case "purchase" -> "\u25C8";
			case "experience" -> "\u25B6";
			case "avatar" -> "\u25C6";
			case "robux" -> "\u25C8";
			case "fail" -> "\u2716";
			default -> "\u25CF";
		};
	}

	public boolean isExpired() {
		return age > lifetime;
	}

	public float alpha() {
		if (age < 8) {
			return age / 8.0F;
		}
		int remaining = lifetime - age;
		if (remaining < 20) {
			return Math.max(0.0F, remaining / 20.0F);
		}
		return 1.0F;
	}

	public float slide() {
		return age < 8 ? 1.0F - age / 8.0F : 0.0F;
	}
}

package com.robloxify.client.notification;

import com.robloxify.client.ClientSounds;
import com.robloxify.net.RobloxifyPayloads;
import com.robloxify.sound.RobloxifySounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Roblox style notification stack. Drawn as a small set of rectangles and text, so it costs
 * essentially nothing while there is nothing to show.
 */
public final class NotificationManager {
	private static final int MAX = 4;
	private static final int WIDTH = 168;
	private static final int HEIGHT = 30;
	private static final int MARGIN = 6;

	private static final List<RobloxNotification> ACTIVE = new ArrayList<>();

	private NotificationManager() {
	}

	public static void push(RobloxifyPayloads.Notification payload) {
		push(new RobloxNotification(payload.kind(), payload.title(), payload.subtitle(), payload.robuxReward()));
	}

	public static void push(RobloxNotification notification) {
		if (ACTIVE.size() >= MAX) {
			ACTIVE.remove(0);
		}
		ACTIVE.add(notification);
		ClientSounds.play(switch (notification.kind) {
			case "badge" -> RobloxifySounds.BADGE_UNLOCK;
			case "complete" -> RobloxifySounds.EXPERIENCE_COMPLETE;
			case "purchase" -> RobloxifySounds.PURCHASE;
			case "checkpoint" -> RobloxifySounds.CHECKPOINT;
			case "fail" -> RobloxifySounds.FAIL;
			case "robux" -> RobloxifySounds.LEVEL_UP;
			default -> RobloxifySounds.UI_OPEN;
		}, 1.0F);
	}

	public static void tick() {
		if (ACTIVE.isEmpty()) {
			return;
		}
		ACTIVE.removeIf(notification -> {
			notification.age++;
			return notification.isExpired();
		});
	}

	public static boolean isEmpty() {
		return ACTIVE.isEmpty();
	}

	public static void render(GuiGraphicsExtractor graphics, Font font, int screenWidth) {
		if (ACTIVE.isEmpty()) {
			return;
		}
		int y = MARGIN + 20;
		for (RobloxNotification notification : ACTIVE) {
			int x = screenWidth - WIDTH - MARGIN + (int) (notification.slide() * (WIDTH + MARGIN));
			renderCard(graphics, font, notification, x, y);
			y += HEIGHT + 4;
		}
	}

	private static void renderCard(GuiGraphicsExtractor graphics, Font font, RobloxNotification notification,
								   int x, int y) {
			int accent = notification.accent();
			graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xE6141420);
			graphics.fill(x, y, x + 3, y + HEIGHT, accent);
			graphics.fill(x, y, x + WIDTH, y + 1, 0x40FFFFFF);

			graphics.text(font, notification.icon(), x + 9, y + 11, accent);
			graphics.text(font, notification.title, x + 22, y + 5, 0xFFFFFFFF);
			graphics.text(font, trim(font, notification.subtitle, WIDTH - 30), x + 22, y + 16, 0xFF9AA0A6);

			if (notification.robux > 0) {
				String reward = "+" + notification.robux;
				graphics.text(font, reward, x + WIDTH - 8 - font.width(reward), y + 5, 0xFFFFC64A);
			}
	}

	private static String trim(Font font, String text, int maxWidth) {
		if (font.width(text) <= maxWidth) {
			return text;
		}
		return font.plainSubstrByWidth(text, maxWidth - 6) + "...";
	}

	public static void clear() {
		ACTIVE.clear();
	}

	public static Minecraft client() {
		return Minecraft.getInstance();
	}
}

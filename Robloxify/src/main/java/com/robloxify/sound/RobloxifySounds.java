package com.robloxify.sound;

import com.robloxify.Robloxify;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Robloxify sound events. The audio files themselves are original placeholders shipped with
 * the mod (see {@code assets/robloxify/sounds.json}).
 */
public final class RobloxifySounds {
	public static final SoundEvent UI_OPEN = register("ui_open");
	public static final SoundEvent UI_CLOSE = register("ui_close");
	public static final SoundEvent PURCHASE = register("purchase");
	public static final SoundEvent BADGE_UNLOCK = register("badge_unlock");
	public static final SoundEvent CHECKPOINT = register("checkpoint");
	public static final SoundEvent EXPERIENCE_COMPLETE = register("experience_complete");
	public static final SoundEvent AVATAR_SWITCH = register("avatar_switch");
	public static final SoundEvent OOF = register("oof");
	public static final SoundEvent BOUNCE = register("bounce");
	public static final SoundEvent UI_HOVER = register("ui_hover");
	public static final SoundEvent EXPERIENCE_START = register("experience_start");
	public static final SoundEvent AVATAR_EQUIP = register("avatar_equip");
	public static final SoundEvent LEVEL_UP = register("level_up");
	public static final SoundEvent RESPAWN = register("respawn");
	public static final SoundEvent FAIL = register("fail");

	private RobloxifySounds() {
	}

	private static SoundEvent register(String path) {
		Identifier id = Robloxify.id(path);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	/** Forces class loading so the static registrations run. */
	public static void bootstrap() {
	}
}

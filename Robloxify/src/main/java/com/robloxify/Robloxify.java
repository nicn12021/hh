package com.robloxify;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (both client and dedicated server) entrypoint of Robloxify.
 */
public class Robloxify implements ModInitializer {
	public static final String MOD_ID = "robloxify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Robloxify initialized!");
	}
}

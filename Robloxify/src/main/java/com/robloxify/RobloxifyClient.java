package com.robloxify;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client-only entrypoint of Robloxify.
 */
public class RobloxifyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Robloxify.LOGGER.info("Robloxify client initialized!");
	}
}

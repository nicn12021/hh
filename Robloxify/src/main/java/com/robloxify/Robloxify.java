package com.robloxify;

import com.robloxify.command.RobloxifyCommands;
import com.robloxify.config.RobloxifyConfig;
import com.robloxify.event.RobloxifyEvents;
import com.robloxify.net.RobloxifyNetworking;
import com.robloxify.sound.RobloxifySounds;
import com.robloxify.world.RobloxifyBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common (client + dedicated server) entrypoint of Robloxify. */
public class Robloxify implements ModInitializer {
	public static final String MOD_ID = "robloxify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String VERSION = FabricLoader.getInstance()
			.getModContainer(MOD_ID)
			.map(container -> container.getMetadata().getVersion().getFriendlyString())
			.orElse("dev");

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		RobloxifyConfig.load();
		RobloxifySounds.bootstrap();
		RobloxifyBlocks.register();
		RobloxifyNetworking.registerPayloads();
		RobloxifyEvents.register();
		RobloxifyCommands.register();
		LOGGER.info("Robloxify initialized!");
	}
}

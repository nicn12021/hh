package com.robloxify.world;

import com.robloxify.Robloxify;
import com.robloxify.world.block.BouncePadBlock;
import com.robloxify.world.block.CheckpointBlock;
import com.robloxify.world.block.DisappearingPlatformBlock;
import com.robloxify.world.block.FinishPadBlock;
import com.robloxify.world.block.SpawnPadBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Every Robloxify block. All of them are placeable and available in Creative. */
public final class RobloxifyBlocks {
	public static final Block STUD = create("stud", Block::new, MapColor.COLOR_LIGHT_GRAY, SoundType.WOOD, 1.0F);
	public static final Block CHECKPOINT = create("checkpoint", CheckpointBlock::new, MapColor.EMERALD, SoundType.METAL, 1.2F);
	public static final Block BOUNCE_PAD = create("bounce_pad", BouncePadBlock::new, MapColor.DIAMOND, SoundType.SLIME_BLOCK, 1.0F);
	public static final Block FINISH_PAD = create("finish_pad", FinishPadBlock::new, MapColor.GOLD, SoundType.METAL, 1.2F);
	public static final Block SPAWN_PAD = create("spawn_pad", SpawnPadBlock::new, MapColor.QUARTZ, SoundType.METAL, 1.0F);
	public static final Block DISAPPEARING_PLATFORM = create("disappearing_platform", DisappearingPlatformBlock::new,
			MapColor.COLOR_MAGENTA, SoundType.GLASS, 0.6F);

	private static final List<ResourceKey<Block>> BLOCK_KEYS = new ArrayList<>();
	private static final List<Block> BLOCKS = new ArrayList<>();
	private static final List<String> NAMES = new ArrayList<>();
	private static final List<Item> ITEMS = new ArrayList<>();

	private RobloxifyBlocks() {
	}

	private static Block create(String name, Function<BlockBehaviour.Properties, Block> factory,
								MapColor color, SoundType sound, float strength) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Robloxify.id(name));
		Block block = factory.apply(BlockBehaviour.Properties.of()
				.setId(key)
				.mapColor(color)
				.strength(strength)
				.sound(sound)
				.pushReaction(PushReaction.NORMAL));
		BLOCK_KEYS.add(key);
		BLOCKS.add(block);
		NAMES.add(name);
		return block;
	}

	public static void register() {
		for (int i = 0; i < BLOCKS.size(); i++) {
			Registry.register(BuiltInRegistries.BLOCK, BLOCK_KEYS.get(i), BLOCKS.get(i));
			ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Robloxify.id(NAMES.get(i)));
			Item item = new BlockItem(BLOCKS.get(i), new Item.Properties().setId(itemKey).useItemDescriptionPrefix());
			ITEMS.add(item);
			Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		}
	}

	/** Used to put everything into a Creative tab. */
	public static List<Item> items() {
		return ITEMS;
	}

	public static boolean isExperienceBlock(Block block) {
		return block == CHECKPOINT || block == BOUNCE_PAD || block == FINISH_PAD
				|| block == SPAWN_PAD || block == DISAPPEARING_PLATFORM;
	}
}

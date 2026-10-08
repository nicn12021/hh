package com.robloxify.world;

import com.robloxify.Robloxify;
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

/** The famous stud. A secret little block that pays out Robux when placed. */
public final class RobloxifyBlocks {
	public static final ResourceKey<Block> STUD_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, Robloxify.id("stud"));
	public static final ResourceKey<Item> STUD_ITEM_KEY = ResourceKey.create(Registries.ITEM, Robloxify.id("stud"));

	public static final Block STUD = new Block(BlockBehaviour.Properties.of()
			.setId(STUD_BLOCK_KEY)
			.mapColor(MapColor.COLOR_LIGHT_GRAY)
			.strength(1.0F)
			.sound(SoundType.WOOD));

	private RobloxifyBlocks() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.BLOCK, STUD_BLOCK_KEY, STUD);
		Registry.register(BuiltInRegistries.ITEM, STUD_ITEM_KEY, new BlockItem(STUD,
				new Item.Properties().setId(STUD_ITEM_KEY).useItemDescriptionPrefix()));
	}
}

package com.robloxify.world;

import com.robloxify.Robloxify;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** The famous stud. A secret little block that pays out a single Robux when placed. */
public final class RobloxifyBlocks {
	public static final Block STUD = new Block(BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_GRAY)
			.strength(1.0F)
			.sound(SoundType.WOOD));

	private RobloxifyBlocks() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.BLOCK, Robloxify.id("stud"), STUD);
		Registry.register(BuiltInRegistries.ITEM, Robloxify.id("stud"), new BlockItem(STUD, new Item.Properties()));
	}
}

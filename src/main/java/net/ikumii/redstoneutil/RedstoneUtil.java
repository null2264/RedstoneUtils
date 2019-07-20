package net.ikumii.redstoneutil;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RedstoneUtil implements ModInitializer
{
	public static final Block BREAKER = new BreakerBlock();
	public static final Block DETECTOR = new DetectorBlock();
	public static final Block CLOCK = new ClockBlock();
	
	public static final String modid = "redstoneutils";
	
	@Override
	public void onInitialize()
	{
		// Register Blocks
		Registry.register(Registry.BLOCK, createID("breaker"), BREAKER);
		Registry.register(Registry.BLOCK, createID("detector"), DETECTOR);
		Registry.register(Registry.BLOCK, createID("clock"), CLOCK);
		// Register BlockItems and Items
		Registry.register(Registry.ITEM, createID("breaker"), new BlockItem(BREAKER, new Item.Settings().group(ItemGroup.REDSTONE)));
		Registry.register(Registry.ITEM, createID("detector"), new BlockItem(DETECTOR, new Item.Settings().group(ItemGroup.REDSTONE)));
		Registry.register(Registry.ITEM, createID("clock"), new BlockItem(CLOCK, new Item.Settings().group(ItemGroup.REDSTONE)));
	}
	
	private Identifier createID(String name)
	{
		return new Identifier(modid, name);
	}
}

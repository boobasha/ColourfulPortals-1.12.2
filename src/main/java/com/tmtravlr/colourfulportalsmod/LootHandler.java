package com.tmtravlr.colourfulportalsmod;

import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ColourfulPortalsMod.MODID)
public class LootHandler {

	@SubscribeEvent
	public static void onLootLoad(LootTableLoadEvent event) {
		if (!ColourfulPortalsMod.addColourfulWaterToDungeonChests) {
			return;
		}
		String name = event.getName().toString();
		if (!(name.equals("minecraft:chests/simple_dungeon") || name.equals("minecraft:chests/stronghold_corridor")
				|| name.equals("minecraft:chests/stronghold_crossing") || name.equals("minecraft:chests/desert_pyramid")
				|| name.equals("minecraft:chests/jungle_temple"))) {
			return;
		}
		LootPool pool = event.getTable().getPool("main");
		if (pool == null) {
			return;
		}
		try {
			pool.addEntry(new LootEntryItem(ColourfulPortalsMod.bucketColourfulWaterEmpty, 3, 0,
					new LootFunction[] { new SetCount(new LootCondition[0], new RandomValueRange(1.0F)) }, new LootCondition[0], "colourfulportalsmod:empty_bucket"));
			pool.addEntry(new LootEntryItem(ColourfulPortalsMod.bucketColourfulWater, 2, 0,
					new LootFunction[] { new SetCount(new LootCondition[0], new RandomValueRange(1.0F)) }, new LootCondition[0], "colourfulportalsmod:full_bucket"));
		} catch (Exception e) {
			// Defensive: never let loot injection crash chest generation.
		}
	}
}

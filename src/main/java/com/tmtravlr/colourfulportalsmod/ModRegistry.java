package com.tmtravlr.colourfulportalsmod;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ColourfulPortalsMod.MODID)
public class ModRegistry {

	@SubscribeEvent
	@SuppressWarnings("deprecation")
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		ColourfulPortalsMod.loadConfig();
		ColourfulPortalsMod.ensureFluid();
		IForgeRegistry<Block> reg = event.getRegistry();

		Block water = new BlockColourfulWater();
		water.setHardness(100.0F);
		water.setLightOpacity(3);
		ColourfulPortalsMod.colourfulWater = water;
		reg.register(water);

		ColourfulPortalsMod.cpBlocks.clear();
		ColourfulPortalsMod.scpBlocks.clear();
		ColourfulPortalsMod.frameBlocks.clear();
		ColourfulPortalsMod.frameNames.clear();

		int index = 0;
		for (int i = 0; i < ColourfulPortalsMod.frameBlockNames.length; i++) {
			String name = ColourfulPortalsMod.frameBlockNames[i];
			Block frameBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
			if (frameBlock == null || frameBlock == Blocks.AIR) {
				System.err.println("[Colourful Portals] Error! Couldn't find a block with name '" + name + "'!");
				continue;
			}

			String frameName;
			int colon = name.indexOf(':');
			if (colon != -1) {
				frameName = name.substring(0, colon) + "_" + name.substring(colon + 1);
			} else {
				frameName = name;
			}

			BlockColourfulPortal cp = new BlockColourfulPortal(Material.PORTAL, SoundType.GLASS);
			cp.setHardness(-1.0F);
			cp.setLightLevel(0.75F);
			cp.setUnlocalizedName("colourfulPortal");
			cp.setRegistryName(ColourfulPortalsMod.MODID, "cp_" + frameName);

			BlockStandaloneCP scp = new BlockStandaloneCP(frameBlock.getDefaultState().getMaterial(), frameBlock.getSoundType());
			scp.setHardness(0.8F);
			scp.setLightLevel(0.75F);
			scp.setUnlocalizedName("standaloneColourfulPortal");
			scp.setRegistryName(ColourfulPortalsMod.MODID, "scp_" + frameName);

			ColourfulPortalsMod.cpBlocks.put(index, cp);
			ColourfulPortalsMod.scpBlocks.put(index, scp);
			ColourfulPortalsMod.frameBlocks.put(index, frameBlock);
			ColourfulPortalsMod.frameNames.put(index, frameName);

			reg.register(cp);
			reg.register(scp);
			index++;
		}
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();

		ColourfulPortalsMod.bucketColourfulWaterEmpty = makeBucket(new ItemBucketColourfulWater(true, true, false), "bucketColourfulWaterEmpty", "bucket_colourful_water_empty");
		ColourfulPortalsMod.bucketColourfulWater = makeBucket(new ItemBucketColourfulWater(true, true, true), "bucketColourfulWater", "bucket_colourful_water");
		ColourfulPortalsMod.bucketColourfulWaterUnmixed = makeBucket(new ItemBucketColourfulWater(false, false, true), "bucketColourfulWaterUnmixed", "bucket_colourful_water_unmixed");
		ColourfulPortalsMod.bucketColourfulWaterPartMixed = makeBucket(new ItemBucketColourfulWater(true, false, true), "bucketColourfulWaterPartMixed", "bucket_colourful_water_part_mixed");
		ColourfulPortalsMod.bucketColourfulWaterFirst = makeBucket(new ItemBucketColourfulWater(false, true, false), "bucketColourfulWaterFirst", "bucket_colourful_water_first");

		Item pearl = new ItemEnderPearlColoured(false);
		pearl.setRegistryName(ColourfulPortalsMod.MODID, "colourful_ender_pearl");
		ColourfulPortalsMod.enderPearlColoured = pearl;
		Item pearlR = new ItemEnderPearlColoured(true);
		pearlR.setRegistryName(ColourfulPortalsMod.MODID, "colourful_ender_pearl_reflective");
		ColourfulPortalsMod.enderPearlColouredReflective = pearlR;

		reg.register(ColourfulPortalsMod.bucketColourfulWaterEmpty);
		reg.register(ColourfulPortalsMod.bucketColourfulWater);
		reg.register(ColourfulPortalsMod.bucketColourfulWaterUnmixed);
		reg.register(ColourfulPortalsMod.bucketColourfulWaterPartMixed);
		reg.register(ColourfulPortalsMod.bucketColourfulWaterFirst);
		reg.register(pearl);
		reg.register(pearlR);

		// ItemBlocks for the standalone portals (framed portals are created in-world only, no item).
		for (int i = 0; i < ColourfulPortalsMod.scpBlocks.size(); i++) {
			BlockStandaloneCP scp = ColourfulPortalsMod.scpBlocks.get(i);
			ItemStandaloneCP item = new ItemStandaloneCP(scp);
			item.setRegistryName(scp.getRegistryName());
			reg.register(item);
		}
	}

	private static Item makeBucket(ItemBucketColourfulWater bucket, String unlocalized, String registry) {
		bucket.setUnlocalizedName(unlocalized);
		bucket.setRegistryName(ColourfulPortalsMod.MODID, registry);
		return bucket;
	}

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		ResourceLocation rl = new ResourceLocation(ColourfulPortalsMod.MODID, "teleport");
		SoundEvent se = new SoundEvent(rl);
		se.setRegistryName(rl);
		ColourfulPortalsMod.soundTeleport = se;
		event.getRegistry().register(se);
	}

	private static int recipeCounter = 0;

	private static void add(IForgeRegistry<IRecipe> reg, IRecipe recipe, String name) {
		recipe.setRegistryName(new ResourceLocation(ColourfulPortalsMod.MODID, name));
		reg.register(recipe);
	}

	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		IForgeRegistry<IRecipe> reg = event.getRegistry();
		ResourceLocation group = new ResourceLocation(ColourfulPortalsMod.MODID, "colourful_portals");

		// Standalone portal recipes: a ring of the coloured frame block around a full bucket.
		for (int f = 0; f < ColourfulPortalsMod.frameBlocks.size(); f++) {
			Block frameBlock = ColourfulPortalsMod.frameBlocks.get(f);
			BlockStandaloneCP scp = ColourfulPortalsMod.scpBlocks.get(f);
			String frameName = ColourfulPortalsMod.frameNames.get(f);
			for (int i = 0; i < 16; i++) {
				ItemStack frame = new ItemStack(frameBlock, 1, i);
				ItemStack output = new ItemStack(scp, 1, i);
				add(reg, new ShapedOreRecipe(group, output, "WWW", "WBW", "WWW", 'W', frame, 'B', new ItemStack(ColourfulPortalsMod.bucketColourfulWater)),
						"scp_" + frameName + "_" + i);
			}
		}

		add(reg, new ShapelessOreRecipe(group, new ItemStack(ColourfulPortalsMod.bucketColourfulWaterPartMixed, 1),
				Items.WATER_BUCKET, new ItemStack(ColourfulPortalsMod.bucketColourfulWaterEmpty), "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyeYellow", "dyeWhite"),
				"bucket_part_mixed");

		add(reg, new ShapelessOreRecipe(group, new ItemStack(ColourfulPortalsMod.bucketColourfulWaterUnmixed, 1),
				Items.WATER_BUCKET, new ItemStack(ColourfulPortalsMod.bucketColourfulWaterFirst), "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyeYellow", "dyeWhite"),
				"bucket_unmixed");

		add(reg, new ShapelessOreRecipe(group, new ItemStack(ColourfulPortalsMod.enderPearlColoured, 1),
				Items.ENDER_PEARL, "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyeYellow", "dyeWhite"),
				"coloured_ender_pearl");

		add(reg, new ShapedOreRecipe(group, new ItemStack(ColourfulPortalsMod.bucketColourfulWaterFirst, 1), "G", "B", 'G', Items.GOLD_INGOT, 'B', Items.BUCKET),
				"bucket_first_a");

		add(reg, new ShapedOreRecipe(group, new ItemStack(ColourfulPortalsMod.bucketColourfulWaterFirst, 1), "IGI", " I ", 'G', Items.GOLD_INGOT, 'I', Items.IRON_INGOT),
				"bucket_first_b");

		add(reg, new ShapedOreRecipe(group, new ItemStack(ColourfulPortalsMod.enderPearlColouredReflective, 1), " Q ", "QPQ", " Q ", 'Q', Items.QUARTZ, 'P', new ItemStack(ColourfulPortalsMod.enderPearlColoured)),
				"reflective_pearl");

		add(reg, new ShapelessOreRecipe(group, new ItemStack(ColourfulPortalsMod.bucketColourfulWaterFirst), new ItemStack(ColourfulPortalsMod.bucketColourfulWaterEmpty)),
				"empty_to_first");

		add(reg, new ShapelessOreRecipe(group, new ItemStack(Items.BUCKET), new ItemStack(ColourfulPortalsMod.bucketColourfulWaterFirst)),
				"first_to_bucket");

		if (ColourfulPortalsMod.xpBottleCrafting) {
			add(reg, new ShapelessOreRecipe(group, new ItemStack(ColourfulPortalsMod.bucketColourfulWater),
					new ItemStack(ColourfulPortalsMod.bucketColourfulWaterUnmixed),
					new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.EXPERIENCE_BOTTLE),
					new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.EXPERIENCE_BOTTLE)),
					"xp_unmixed");
			add(reg, new ShapelessOreRecipe(group, new ItemStack(ColourfulPortalsMod.bucketColourfulWater),
					new ItemStack(ColourfulPortalsMod.bucketColourfulWaterPartMixed),
					new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.EXPERIENCE_BOTTLE)),
					"xp_part_mixed");
		}
	}
}

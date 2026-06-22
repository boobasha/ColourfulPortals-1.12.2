package com.tmtravlr.colourfulportalsmod;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ColourfulPortalsMod.MODID)
public class ClientRegistry {

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		reg(ColourfulPortalsMod.bucketColourfulWaterEmpty);
		reg(ColourfulPortalsMod.bucketColourfulWater);
		reg(ColourfulPortalsMod.bucketColourfulWaterUnmixed);
		reg(ColourfulPortalsMod.bucketColourfulWaterPartMixed);
		reg(ColourfulPortalsMod.bucketColourfulWaterFirst);
		reg(ColourfulPortalsMod.enderPearlColoured);
		reg(ColourfulPortalsMod.enderPearlColouredReflective);

		// Standalone portal items use a builtin/entity model rendered by TEISRStandaloneCP.
		for (int f = 0; f < ColourfulPortalsMod.scpBlocks.size(); f++) {
			BlockStandaloneCP scp = ColourfulPortalsMod.scpBlocks.get(f);
			Item item = Item.getItemFromBlock(scp);
			ModelResourceLocation inv = new ModelResourceLocation(scp.getRegistryName(), "inventory");
			for (int i = 0; i < 16; i++) {
				ModelLoader.setCustomModelResourceLocation(item, i, inv);
			}
		}

		final ModelResourceLocation fluidLocation = new ModelResourceLocation(ColourfulPortalsMod.MODID + ":colourful_water", "fluid");
		ModelLoader.setCustomStateMapper(ColourfulPortalsMod.colourfulWater, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return fluidLocation;
			}
		});
	}

	private static void reg(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

	@SubscribeEvent
	public static void registerBlockColors(ColorHandlerEvent.Block event) {
		BlockColors blockColors = event.getBlockColors();
		IBlockColor handler = new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				int meta = state.getBlock().getMetaFromState(state);
				return EnumDyeColor.byMetadata(meta).getColorValue();
			}
		};
		for (int f = 0; f < ColourfulPortalsMod.scpBlocks.size(); f++) {
			blockColors.registerBlockColorHandler(handler, ColourfulPortalsMod.scpBlocks.get(f));
		}
	}

	@SubscribeEvent
	public static void registerItemColors(ColorHandlerEvent.Item event) {
		ItemColors itemColors = event.getItemColors();
		IItemColor handler = new IItemColor() {
			@Override
			public int colorMultiplier(ItemStack stack, int tintIndex) {
				return EnumDyeColor.byMetadata(stack.getMetadata()).getColorValue();
			}
		};
		for (int f = 0; f < ColourfulPortalsMod.scpBlocks.size(); f++) {
			Block scp = ColourfulPortalsMod.scpBlocks.get(f);
			itemColors.registerItemColorHandler(handler, scp);
		}
	}
}

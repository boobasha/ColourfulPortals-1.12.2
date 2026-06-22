package com.tmtravlr.colourfulportalsmod;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DispenserBehaviorCP {

	public static void register() {
		// Empty stain-proof bucket: pick up a source block of colourful water.
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ColourfulPortalsMod.bucketColourfulWaterEmpty, new BehaviorDefaultDispenseItem() {
			private final BehaviorDefaultDispenseItem fallback = new BehaviorDefaultDispenseItem();

			@Override
			public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
				EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
				World world = source.getWorld();
				BlockPos pos = source.getBlockPos().offset(facing);
				IBlockState state = world.getBlockState(pos);
				if (state.getBlock() == ColourfulPortalsMod.colourfulWater && state.getBlock().getMetaFromState(state) == 0) {
					world.setBlockToAir(pos);
					ItemStack filled = new ItemStack(ColourfulPortalsMod.bucketColourfulWater);
					stack.shrink(1);
					if (stack.isEmpty()) {
						return filled;
					}
					if (((TileEntityDispenser) source.getBlockTileEntity()).addItemStack(filled) < 0) {
						this.fallback.dispense(source, filled);
					}
					return stack;
				}
				return super.dispenseStack(source, stack);
			}
		});

		// Full stain-proof bucket: place a source block of colourful water.
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ColourfulPortalsMod.bucketColourfulWater, new BehaviorDefaultDispenseItem() {
			private final BehaviorDefaultDispenseItem fallback = new BehaviorDefaultDispenseItem();

			@Override
			public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
				EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
				World world = source.getWorld();
				BlockPos pos = source.getBlockPos().offset(facing);
				if (world.isAirBlock(pos)) {
					world.setBlockState(pos, ColourfulPortalsMod.colourfulWater.getDefaultState());
					ItemStack empty = new ItemStack(ColourfulPortalsMod.bucketColourfulWaterEmpty);
					stack.shrink(1);
					if (stack.isEmpty()) {
						return empty;
					}
					if (((TileEntityDispenser) source.getBlockTileEntity()).addItemStack(empty) < 0) {
						this.fallback.dispense(source, empty);
					}
					return stack;
				}
				return super.dispenseStack(source, stack);
			}
		});
	}
}

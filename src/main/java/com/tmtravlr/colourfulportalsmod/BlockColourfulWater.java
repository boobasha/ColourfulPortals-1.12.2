package com.tmtravlr.colourfulportalsmod;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;

public class BlockColourfulWater extends BlockFluidClassic {

	public BlockColourfulWater() {
		super(ColourfulPortalsMod.colourfulFluid, Material.WATER);
		this.setUnlocalizedName("colourfulWater");
		this.setRegistryName(ColourfulPortalsMod.MODID, "colourful_water");
		ColourfulPortalsMod.colourfulFluid.setBlock(this);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		super.updateTick(world, pos, state, rand);
		BlockPos below = pos.down();
		if ((this.getMetaFromState(state) == 0) && (world.getBlockState(pos).getBlock() == this)
				&& ColourfulPortalsMod.isFrameBlock(world.getBlockState(below).getBlock())
				&& ColourfulPortalsMod.canCreatePortal(world, pos, ColourfulPortalsMod.getCPBlockByFrameBlock(world.getBlockState(below).getBlock()),
						world.getBlockState(below).getBlock().getMetaFromState(world.getBlockState(below)))) {
			BlockColourfulPortal.tryToCreatePortal(world, pos);
		}
	}

	@Override
	public boolean displaceIfPossible(World world, BlockPos pos) {
		if (world.getBlockState(pos).getMaterial() == Material.WATER) {
			return false;
		}
		return super.displaceIfPossible(world, pos);
	}
}

package com.tmtravlr.colourfulportalsmod;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Marker tile entity so the standalone portal can be drawn by a TileEntitySpecialRenderer
 * (the 1.7.10 mod used a custom ISimpleBlockRenderingHandler, which no longer exists).
 * All render data (colour / frame block) is read from the block state, so no NBT is stored.
 */
public class TileEntityStandaloneCP extends TileEntity {

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos()).grow(0.5D, 0.5D, 0.5D);
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 0;
	}
}

package com.tmtravlr.colourfulportalsmod;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TESRStandaloneCP extends TileEntitySpecialRenderer<TileEntityStandaloneCP> {

	@Override
	public void render(TileEntityStandaloneCP te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		World world = te.getWorld();
		if (world == null) {
			return;
		}
		IBlockState state = world.getBlockState(te.getPos());
		Block block = state.getBlock();
		if (!ColourfulPortalsMod.isStandaloneCPBlock(block)) {
			return;
		}
		int meta = block.getMetaFromState(state);
		Block frameBlock = ColourfulPortalsMod.getFrameBlockByShiftedMetadata(ColourfulPortalsMod.getShiftedCPMetadata(block, meta));

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		StandaloneRender.draw(frameBlock, meta, true);
		GlStateManager.popMatrix();
	}
}

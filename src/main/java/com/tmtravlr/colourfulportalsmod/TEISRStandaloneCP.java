package com.tmtravlr.colourfulportalsmod;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renders the standalone portal item (inventory / hand / dropped) with the same geometry as the
 * in-world block, so it shows the recessed pool with the correct frame texture and colour.
 * RenderItem already translates by (-0.5,-0.5,-0.5) before calling this, so the [0,1] geometry is centred.
 */
@SideOnly(Side.CLIENT)
public class TEISRStandaloneCP extends TileEntityItemStackRenderer {

	@Override
	public void renderByItem(ItemStack stack) {
		Block block = Block.getBlockFromItem(stack.getItem());
		if (!(block instanceof BlockStandaloneCP)) {
			return;
		}
		int meta = stack.getMetadata();
		Block frameBlock = ColourfulPortalsMod.getFrameBlockByShiftedMetadata(ColourfulPortalsMod.getShiftedCPMetadata(block, meta));

		GlStateManager.pushMatrix();
		StandaloneRender.draw(frameBlock, meta, false);
		GlStateManager.popMatrix();
	}
}

package com.tmtravlr.colourfulportalsmod;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerEventHandlers() {
		super.registerEventHandlers();
	}

	@Override
	public void registerRenderers() {
		net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStandaloneCP.class, new TESRStandaloneCP());

		TEISRStandaloneCP teisr = new TEISRStandaloneCP();
		for (int f = 0; f < ColourfulPortalsMod.scpBlocks.size(); f++) {
			net.minecraft.item.Item item = net.minecraft.item.Item.getItemFromBlock(ColourfulPortalsMod.scpBlocks.get(f));
			if (item != null) {
				item.setTileEntityItemStackRenderer(teisr);
			}
		}
	}
}

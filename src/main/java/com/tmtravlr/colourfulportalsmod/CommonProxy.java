package com.tmtravlr.colourfulportalsmod;

import java.io.File;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

	public void registerEventHandlers() {
		MinecraftForge.EVENT_BUS.register(new ServerTickEvents());
	}

	/** Client-only renderer binding; no-op on the server. */
	public void registerRenderers() {
	}

	/** Location of the portal-list save file inside the current world's save folder. */
	public File getSaveLocation() {
		File dir = DimensionManager.getCurrentSaveRootDirectory();
		if (dir == null) {
			return null;
		}
		return new File(dir, "colourful_portal_locations.dat");
	}
}

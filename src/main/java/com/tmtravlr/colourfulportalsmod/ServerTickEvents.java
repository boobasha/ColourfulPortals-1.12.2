package com.tmtravlr.colourfulportalsmod;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ServerTickEvents {

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.START) {
			return;
		}
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		ColourfulPortalsMod cpMod = ColourfulPortalsMod.instance;
		if (server == null || cpMod == null) {
			return;
		}
		String folder = server.getFolderName();
		if (folder == null) {
			folder = "";
		}
		if (!folder.equals(cpMod.currentFolder)) {
			cpMod.loadPortalsList();
			cpMod.currentFolder = folder;
		}
		BlockColourfulPortal.serverTick();
	}
}

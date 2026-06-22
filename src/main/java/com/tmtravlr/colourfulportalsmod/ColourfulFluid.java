package com.tmtravlr.colourfulportalsmod;

import net.minecraft.item.EnumRarity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class ColourfulFluid extends Fluid {

	public ColourfulFluid() {
		super("colourful_fluid",
				new ResourceLocation(ColourfulPortalsMod.MODID, "blocks/colourful_water"),
				new ResourceLocation(ColourfulPortalsMod.MODID, "blocks/colourful_water_flow"));
		this.setUnlocalizedName("ColourfulWater");
		this.setLuminosity(5);
		this.setRarity(EnumRarity.RARE);
	}
}

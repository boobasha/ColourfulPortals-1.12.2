package com.tmtravlr.colourfulportalsmod;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class ItemStandaloneCP extends ItemBlock {

	public ItemStandaloneCP(Block block) {
		super(block);
		this.setMaxDamage(0);
		this.setMaxStackSize(16);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, World world, List<String> list, ITooltipFlag flag) {
		String frameDisplayName = "?";
		try {
			Block frame = ColourfulPortalsMod.getFrameBlockByShiftedMetadata(
					ColourfulPortalsMod.getShiftedCPMetadata(Block.getBlockFromItem(itemStack.getItem()), itemStack.getItemDamage()));
			frameDisplayName = new ItemStack(frame, 1, itemStack.getItemDamage()).getDisplayName();
		} catch (Exception e) {
			// Ignore; fall back to "?"
		}
		list.add(TextFormatting.ITALIC + frameDisplayName);
	}
}

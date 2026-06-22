package com.tmtravlr.colourfulportalsmod;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class ItemEnderPearlColoured extends ItemEnderPearl {

	private final boolean isReflective;

	public ItemEnderPearlColoured(boolean reflective) {
		this.isReflective = reflective;
		this.setUnlocalizedName(reflective ? "colourfulEnderPearlReflective" : "colourfulEnderPearl");
		this.setMaxStackSize(1);
		this.setCreativeTab(ColourfulPortalsMod.cpTab);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, World world, List<String> list, ITooltipFlag flag) {
		list.add(TextFormatting.ITALIC + net.minecraft.util.text.translation.I18n.translateToLocal("item.colourfulEnderPearl.info.1"));
		list.add(TextFormatting.ITALIC + net.minecraft.util.text.translation.I18n.translateToLocal("item.colourfulEnderPearl.info.2"));
		list.add(TextFormatting.ITALIC + net.minecraft.util.text.translation.I18n.translateToLocal(this.isReflective ? "item.colourfulEnderPearl.info.3.reflective" : "item.colourfulEnderPearl.info.3"));
		list.add(TextFormatting.ITALIC + net.minecraft.util.text.translation.I18n.translateToLocal("item.colourfulEnderPearl.info.4"));
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (ColourfulPortalsMod.isCPBlock(world.getBlockState(pos).getBlock())) {
			// Creating a destination runs an expensive world scan; rate-limit it so spam-clicking
			// can't flood the server thread and freeze it. The cooldown must be CHECKED (not just set)
			// because onItemUse is not gated by the cooldown automatically.
			if (player.getCooldownTracker().hasCooldown(this)) {
				return EnumActionResult.SUCCESS;
			}
			player.getCooldownTracker().setCooldown(this, 30);
			if (!world.isRemote) {
				BlockColourfulPortal.tryToCreateDestination(world, pos, !this.isReflective);
				player.getHeldItem(hand).shrink(1);
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}
}

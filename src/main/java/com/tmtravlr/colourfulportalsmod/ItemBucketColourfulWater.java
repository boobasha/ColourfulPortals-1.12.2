package com.tmtravlr.colourfulportalsmod;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class ItemBucketColourfulWater extends Item {

	private final boolean isEnchanted;
	private final boolean isMixed;
	private final boolean isFull;

	public ItemBucketColourfulWater(boolean setIsEnchanted, boolean setIsMixed, boolean setIsFull) {
		this.isEnchanted = setIsEnchanted;
		this.isMixed = setIsMixed;
		this.isFull = setIsFull;
		this.setMaxStackSize(this.isFull ? 1 : 16);
		this.setCreativeTab(ColourfulPortalsMod.cpTab);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return this.isEnchanted;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, World world, List<String> list, ITooltipFlag flag) {
		if (!this.isMixed) {
			list.add(TextFormatting.ITALIC + net.minecraft.util.text.translation.I18n.translateToLocal("item.bucketColourfulWaterUnmixed.info.1"));
			list.add(TextFormatting.ITALIC + String.valueOf(this.isEnchanted ? ColourfulPortalsMod.xpLevelRemixingCost : ColourfulPortalsMod.xpLevelMixingCost)
					+ net.minecraft.util.text.translation.I18n.translateToLocal("item.bucketColourfulWaterUnmixed.info.2"));
		}
		if (this.isMixed && !this.isEnchanted && !this.isFull) {
			list.add(TextFormatting.ITALIC + net.minecraft.util.text.translation.I18n.translateToLocal("item.bucketColourfulWaterFirst.info.1"));
		}
		if (this.isEnchanted) {
			list.add(net.minecraft.util.text.translation.I18n.translateToLocal("item.bucketColourfulWater.info.enchant"));
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemStack = player.getHeldItem(hand);

		if (this.isMixed && this.isEnchanted) {
			boolean flag = !this.isFull;
			RayTraceResult ray = this.rayTrace(world, player, flag);
			if (ray == null) {
				return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStack);
			}
			FillBucketEvent event = new FillBucketEvent(player, itemStack, world, ray);
			if (MinecraftForge.EVENT_BUS.post(event)) {
				return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStack);
			}
			if (event.getResult() == Event.Result.ALLOW) {
				if (player.capabilities.isCreativeMode) {
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
				}
				itemStack.shrink(1);
				if (itemStack.isEmpty()) {
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, event.getFilledBucket());
				}
				if (!player.inventory.addItemStackToInventory(event.getFilledBucket())) {
					player.dropItem(event.getFilledBucket(), false);
				}
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
			}
			if (ray.typeOfHit == RayTraceResult.Type.BLOCK) {
				BlockPos pos = ray.getBlockPos();
				if (!world.isBlockModifiable(player, pos)) {
					return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStack);
				}
				if (flag) {
					if (!player.canPlayerEdit(pos, ray.sideHit, itemStack)) {
						return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStack);
					}
					IBlockState state = world.getBlockState(pos);
					if (Block.getIdFromBlock(state.getBlock()) == Block.getIdFromBlock(ColourfulPortalsMod.colourfulWater)) {
						world.setBlockToAir(pos);
						if (player.capabilities.isCreativeMode) {
							return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
						}
						itemStack.shrink(1);
						ItemStack filled = new ItemStack(ColourfulPortalsMod.bucketColourfulWater);
						if (itemStack.isEmpty()) {
							return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, filled);
						}
						if (!player.inventory.addItemStackToInventory(filled)) {
							player.dropItem(filled, false);
						}
						return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
					}
				} else {
					if (!this.isFull) {
						return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, new ItemStack(ColourfulPortalsMod.bucketColourfulWaterEmpty));
					}
					BlockPos placePos = pos.offset(ray.sideHit);
					if (!player.canPlayerEdit(placePos, ray.sideHit, itemStack)) {
						return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStack);
					}
					if (this.tryPlaceContainedColourfulLiquid(world, placePos) && !player.capabilities.isCreativeMode) {
						return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, new ItemStack(ColourfulPortalsMod.bucketColourfulWaterEmpty));
					}
				}
			}
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStack);
		}

		if (!this.isMixed) {
			int xpRequirement = this.isEnchanted ? ColourfulPortalsMod.xpLevelRemixingCost : ColourfulPortalsMod.xpLevelMixingCost;
			if (player.experienceLevel >= xpRequirement) {
				player.addExperienceLevel(-xpRequirement);
				ItemStack toReturn = new ItemStack(ColourfulPortalsMod.bucketColourfulWater);
				itemStack.shrink(1);
				if (itemStack.isEmpty()) {
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, toReturn);
				}
				if (!player.inventory.addItemStackToInventory(toReturn)) {
					player.dropItem(toReturn, false);
				}
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
			}
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStack);
		}

		return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStack);
	}

	public boolean tryPlaceContainedColourfulLiquid(World world, BlockPos pos) {
		if (!this.isFull) {
			return false;
		}
		IBlockState state = world.getBlockState(pos);
		if ((!world.isAirBlock(pos) && state.getMaterial().isSolid()) || ColourfulPortalsMod.isCPBlock(state.getBlock())) {
			return false;
		}
		boolean hasCreatedPortal = false;
		BlockPos below = pos.down();
		IBlockState belowState = world.getBlockState(below);
		if (ColourfulPortalsMod.isFrameBlock(belowState.getBlock()) && !ColourfulPortalsMod.isCPBlock(state.getBlock())
				&& ColourfulPortalsMod.canCreatePortal(world, pos, ColourfulPortalsMod.getCPBlockByFrameBlock(belowState.getBlock()),
						belowState.getBlock().getMetaFromState(belowState))) {
			hasCreatedPortal = BlockColourfulPortal.tryToCreatePortal(world, pos);
		}
		if (!hasCreatedPortal) {
			if (world.provider.doesWaterVaporize() && this.isFull) {
				world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
						2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
				for (int i = 0; i < 8; i++) {
					world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
				}
			} else {
				world.setBlockState(pos, this.isFull ? ColourfulPortalsMod.colourfulWater.getDefaultState() : Blocks.AIR.getDefaultState(), 3);
			}
		}
		return true;
	}
}

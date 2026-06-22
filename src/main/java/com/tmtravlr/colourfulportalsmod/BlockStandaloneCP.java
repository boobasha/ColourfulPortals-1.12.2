package com.tmtravlr.colourfulportalsmod;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.tmtravlr.colourfulportalsmod.ColourfulPortalsMod.ColourfulPortalLocation;

public class BlockStandaloneCP extends BlockColourfulPortal {

	public static final double HEIGHT = 0.8D;
	private static final AxisAlignedBB BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, HEIGHT, 1.0D);

	public BlockStandaloneCP(Material material, net.minecraft.block.SoundType sound) {
		super(material, sound);
		this.setCreativeTab(ColourfulPortalsMod.cpTab);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public net.minecraft.tileentity.TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityStandaloneCP();
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(IBlockState state) {
		// Rendered entirely by TESRStandaloneCP (the original used a custom ISimpleBlockRenderingHandler).
		return net.minecraft.util.EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block other, BlockPos fromPos) {
		// Standalone portals do not collapse when neighbours change.
	}

	@Override
	public int quantityDropped(Random rand) {
		return 1;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(this);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return this.getMetaFromState(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess iba, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return BOX;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess iba, BlockPos pos) {
		return BOX;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);
		for (int i = 0; i < 2; i++) {
			float x = pos.getX() + rand.nextFloat() * 2.0F - 0.5F;
			float z = pos.getZ() + rand.nextFloat() * 2.0F - 0.5F;
			float y = pos.getY() + rand.nextFloat() + 0.5F;
			float xVel = rand.nextFloat() * 0.2F;
			float zVel = rand.nextFloat() * 0.2F;
			float yVel = rand.nextFloat() * 0.2F;
			if (rand.nextBoolean()) {
				xVel = -xVel;
			}
			if (rand.nextBoolean()) {
				zVel = -zVel;
			}
			net.minecraft.client.Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleColourfulPortal(world, x, y, z, xVel, yVel, zVel, false));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
		for (int j = 0; j < 16; j++) {
			items.add(new ItemStack(this, 1, j));
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (Block.getIdFromBlock(world.getBlockState(pos).getBlock()) == Block.getIdFromBlock(this)) {
			if (!ColourfulPortalsMod.canCreatePortal(world, pos, world.getBlockState(pos).getBlock(), this.getMetaFromState(state))) {
				Block.spawnAsEntity(world, pos, new ItemStack(this, 1, this.getMetaFromState(state)));
				world.setBlockToAir(pos);
			} else {
				ColourfulPortalsMod.addPortalToList(new ColourfulPortalLocation(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension(),
						ColourfulPortalsMod.getShiftedCPMetadata(world, pos)));
			}
		}
	}

	// breakBlock is inherited from BlockColourfulPortal (deletes the saved portal location, then Block#breakBlock).

	@Override
	public ItemStack getPickBlock(IBlockState state, net.minecraft.util.math.RayTraceResult target, World world, BlockPos pos, net.minecraft.entity.player.EntityPlayer player) {
		return new ItemStack(this, 1, this.getMetaFromState(state));
	}
}

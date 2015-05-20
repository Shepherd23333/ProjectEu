package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.tiles.DMFurnaceTile;
import moze_intel.projecte.gameObjs.tiles.RMFurnaceTile;
import moze_intel.projecte.gameObjs.tiles.TileEmc;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class MatterFurnace extends BlockDirection implements ITileEntityProvider
{
	private String textureName;
	private boolean isActive;
	private boolean isHighTier;
	private static boolean isUpdating;
	private Random rand = new Random();

	public MatterFurnace(boolean active, boolean isRM) 
	{
		super(Material.rock);
		this.setCreativeTab(ObjHandler.cTab);
		isActive = active;
		isHighTier = isRM;
		textureName = isHighTier ? "rm" : "dm";
		this.setUnlocalizedName("pe_" + textureName + "_furnace");
		
		if (isActive) 
		{
			this.setCreativeTab(null);
			this.setLightLevel(0.875F);
		}
	}
	
	@Override
	public float getBlockHardness(World world, BlockPos pos)
	{
		return world.getBlockMetadata(x, y, z) == 0 ? 1000000F : 2000000F;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return isHighTier ? Item.getItemFromBlock(ObjHandler.rmFurnaceOff) : Item.getItemFromBlock(ObjHandler.dmFurnaceOff);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			if (isHighTier)
			{
				player.openGui(PECore.instance, Constants.RM_FURNACE_GUI, world, x, y, z);
			}
			else
			{
				player.openGui(PECore.instance, Constants.DM_FURNACE_GUI, world, x, y, z);
			}
		}
		
		return true;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		if (!isUpdating)
		{
			IInventory tile = (IInventory) world.getTileEntity(x, y, z);
			if (tile == null) return;
			for (int i = 0; i < tile.getSizeInventory(); i++)
			{
				ItemStack stack = tile.getStackInSlot(i);
				
				if (stack == null) 
				{
					continue;
				}
				
				WorldHelper.spawnEntityItem(world, stack, x, y, z);
			}
			
			world.func_147453_f(x, y, z, block);
		}
		
		world.removeTileEntity(x, y, z);
	}
	
	public void updateFurnaceBlockState(boolean isActive, World world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		TileEntity tile = world.getTileEntity(x, y, z);
		isUpdating = true;

		if (isActive)
		{
			if (isHighTier)
				world.setBlock(x, y, z, ObjHandler.rmFurnaceOn);
			else
				world.setBlock(x, y, z, ObjHandler.dmFurnaceOn);
		}
		else
		{
			if (isHighTier)
				world.setBlock(x, y, z, ObjHandler.rmFurnaceOff);
			else
				world.setBlock(x, y, z, ObjHandler.dmFurnaceOff);
		}

		isUpdating = false;
		world.setBlockMetadataWithNotify(x, y, z, meta, 2);

		if (tile != null)
		{
			tile.validate();
			world.setTileEntity(x, y, z, tile);
		}
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entLiving, ItemStack stack)
	{
		setFacingMeta(world, x, y, z, ((EntityPlayer) entLiving));
		
		TileEntity tile = world.getTileEntity(x, y, z);
		
		if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("ProjectEBlock") && tile instanceof TileEmc)
		{
			stack.getTagCompound().setInteger("x", x);
			stack.getTagCompound().setInteger("y", y);
			stack.getTagCompound().setInteger("z", z);
			stack.getTagCompound().setInteger("EMC", 0);
			stack.getTagCompound().setShort("BurnTime", (short) 0);
			stack.getTagCompound().setShort("CookTime", (short) 0);
			
			tile.readFromNBT(stack.getTagCompound());
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		if (isActive)
		{
			int l = world.getBlockMetadata(x, y, z);
			float f = (float) x + 0.5F;
			float f1 = (float) y + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
			float f2 = (float) z + 0.5F;
			float f3 = 0.52F;
			float f4 = rand.nextFloat() * 0.6F - 0.3F;

			if (l == 4)
			{
				world.spawnParticle("smoke", (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 5)
			{
				world.spawnParticle("smoke", (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 2)
			{
				world.spawnParticle("smoke", (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 3)
			{
				world.spawnParticle("smoke", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, BlockPos pos)
	{
		return isHighTier ? Item.getItemFromBlock(ObjHandler.rmFurnaceOff) : Item.getItemFromBlock(ObjHandler.dmFurnaceOff);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) 
	{
		return isHighTier ? new RMFurnaceTile() : new DMFurnaceTile();
	}
}

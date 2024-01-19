package me.shepherd23333.projecte.gameObjs.tiles;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import me.shepherd23333.projecte.api.tile.IEmcAcceptor;
import me.shepherd23333.projecte.api.tile.IEmcProvider;
import me.shepherd23333.projecte.api.tile.TileEmcBase;
import me.shepherd23333.projecte.utils.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Map;

public abstract class TileEmc extends TileEmcBase implements ITickable {
    public TileEmc() {
    }

    public TileEmc(BigInteger maxAmount) {
        setMaximumEMC(maxAmount);
    }

    @Override
    public final NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState state, @Nonnull IBlockState newState) {
        return state.getBlock() != newState.getBlock();
    }

    protected boolean hasMaxedEmc() {
        return getStoredEmc().compareTo(getMaximumEmc()) >= 0;
    }

    /**
     * The amount provided will be divided and evenly distributed as best as possible between adjacent IEMCAcceptors
     * Remainder or rejected EMC is added back to this provider
     *
     * @param emc The maximum combined emc to send to others
     */
    protected void sendToAllAcceptors(BigInteger emc) {
        if (!(this instanceof IEmcProvider)) {
            // todo move this method somewhere
            throw new UnsupportedOperationException("sending without being a provider");
        }


        Map<EnumFacing, TileEntity> tiles = Maps.filterValues(WorldHelper.getAdjacentTileEntitiesMapped(world, this), Predicates.instanceOf(IEmcAcceptor.class));
        if (tiles.isEmpty()) {
            return;
        }

        BigInteger emcPer = emc.divide(BigInteger.valueOf(tiles.size()));
        for (Map.Entry<EnumFacing, TileEntity> entry : tiles.entrySet()) {
            if (this instanceof RelayMK1Tile && entry.getValue() instanceof RelayMK1Tile) {
                continue;
            }
            BigInteger provide = ((IEmcProvider) this).provideEMC(entry.getKey().getOpposite(), emcPer);
            BigInteger remain = provide.subtract(((IEmcAcceptor) entry.getValue()).acceptEMC(entry.getKey(), provide));
            this.addEMC(remain);
        }
    }

    class StackHandler extends ItemStackHandler {
        StackHandler(int size) {
            super(size);
        }

        @Override
        public void onContentsChanged(int slot) {
            TileEmc.this.markDirty();
        }
    }
}

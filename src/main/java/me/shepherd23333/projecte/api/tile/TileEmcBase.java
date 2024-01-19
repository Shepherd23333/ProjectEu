package me.shepherd23333.projecte.api.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.math.BigInteger;

/**
 * Base class for the reference implementations TileEmcProvider, TileEmcAcceptor, and TileEmcHandler
 * Usually you want to use one of three derived reference implementations
 * Extend this if you want fine-grained control over all aspects of how your tile provides or accepts EMC
 *
 * @author williewillus
 */
public class TileEmcBase extends TileEntity implements IEmcStorage {
    protected BigInteger maximumEMC;
    protected BigInteger currentEMC = BigInteger.ZERO;

    protected TileEmcBase() {
    }

    public final void setMaximumEMC(BigInteger max) {
        maximumEMC = max;
        if (currentEMC.compareTo(maximumEMC) > 0) {
            currentEMC = maximumEMC;
        }
    }

    @Override
    public BigInteger getStoredEmc() {
        return currentEMC;
    }

    @Override
    public BigInteger getMaximumEmc() {
        return maximumEMC;
    }

    /**
     * Add EMC directly into the internal buffer. Use for internal implementation of your tile
     */
    protected void addEMC(BigInteger toAdd) {
        currentEMC = currentEMC.add(toAdd);
        if (currentEMC.compareTo(maximumEMC) > 0) {
            currentEMC = maximumEMC;
        }
    }

    /**
     * Removes EMC directly into the internal buffer. Use for internal implementation of your tile
     */
    protected void removeEMC(BigInteger toRemove) {
        currentEMC = currentEMC.subtract(toRemove);
        if (currentEMC.compareTo(BigInteger.ZERO) < 0) {
            currentEMC = BigInteger.ZERO;
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        if (currentEMC.compareTo(maximumEMC) > 0) {
            currentEMC = maximumEMC;
        }
        tag.setString("EMC", currentEMC.toString());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        BigInteger set = new BigInteger(tag.getString("EMC"));
        if (set.compareTo(maximumEMC) > 0) {
            set = maximumEMC;
        }
        currentEMC = set;
    }
}

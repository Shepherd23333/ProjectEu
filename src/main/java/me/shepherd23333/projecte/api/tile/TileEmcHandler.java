package me.shepherd23333.projecte.api.tile;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.math.BigInteger;

/**
 * Reference implementation of both IEMCAcceptor and IEMCProvider
 *
 * @author williewillus
 */
public class TileEmcHandler extends TileEmcBase implements IEmcAcceptor, IEmcProvider {
    public TileEmcHandler() {
    }

    public TileEmcHandler(BigInteger max) {
        this.maximumEMC = max;
    }

    // -- IEMCAcceptor -- //
    @Override
    public BigInteger acceptEMC(@Nonnull EnumFacing side, BigInteger toAccept) {
        BigInteger toAdd = maximumEMC.subtract(currentEMC).min(toAccept);
        currentEMC = currentEMC.add(toAdd);
        return toAdd;
    }

    // -- IEMCProvider -- //
    @Override
    public BigInteger provideEMC(@Nonnull EnumFacing side, BigInteger toExtract) {
        BigInteger toRemove = currentEMC.min(toExtract);
        currentEMC = currentEMC.subtract(toRemove);
        return toRemove;
    }

    // -- IEMCStorage --//
    @Override
    public BigInteger getStoredEmc() {
        return currentEMC;
    }

    @Override
    public BigInteger getMaximumEmc() {
        return maximumEMC;
    }
}

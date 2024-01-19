package me.shepherd23333.projecte.api.tile;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.math.BigInteger;

/**
 * Reference implementation of IEMCAcceptor
 *
 * @author williewillus
 */
public class TileEmcAcceptor extends TileEmcBase implements IEmcAcceptor {
    @Override
    public BigInteger acceptEMC(@Nonnull EnumFacing side, BigInteger toAccept) {
        BigInteger toAdd = maximumEMC.subtract(currentEMC).min(toAccept);
        addEMC(toAdd);
        return toAdd;
    }
}

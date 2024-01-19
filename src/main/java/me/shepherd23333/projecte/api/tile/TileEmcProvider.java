package me.shepherd23333.projecte.api.tile;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.math.BigInteger;

/**
 * Reference implementation for IEMCProvider
 *
 * @author williewillus
 */
public class TileEmcProvider extends TileEmcBase implements IEmcProvider {
    @Override
    public BigInteger provideEMC(@Nonnull EnumFacing side, BigInteger toExtract) {
        BigInteger toRemove = currentEMC.min(toExtract);
        removeEMC(toRemove);
        return toRemove;
    }
}

package me.shepherd23333.projecte.api.tile;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.math.BigInteger;

/**
 * Implement this interface to specify that "EMC can be given to this Tile Entity from an external source"
 * The contract of this interface is only the above statement
 * However, ProjectE implements an "active-push" system, where providers automatically send EMC to acceptors. You are recommended to follow this convention
 * Reference implementation provided in TileEmcHandler
 *
 * @author williewillus
 */
public interface IEmcAcceptor extends IEmcStorage {
    /**
     * Accept, at most, the given amount of EMC from the given side
     *
     * @param side     The side to accept EMC from
     * @param toAccept The maximum amount to accept
     * @return The amount actually accepted
     */
    BigInteger acceptEMC(@Nonnull EnumFacing side, BigInteger toAccept);

}

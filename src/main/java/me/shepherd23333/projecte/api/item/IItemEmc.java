package me.shepherd23333.projecte.api.item;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.math.BigInteger;

/**
 * This interface defines the contract for items that wish to expose their internal EMC storage for external manipulation
 *
 * @author williewillus
 */
public interface IItemEmc {
    /**
     * Adds EMC to the itemstack
     *
     * @param stack The itemstack to add to
     * @param toAdd The maximum amount to add
     * @return The amount that was actually added
     */
    BigInteger addEmc(@Nonnull ItemStack stack, BigInteger toAdd);

    /**
     * Extracts EMC from the itemstack
     *
     * @param stack    The itemstack to remove from
     * @param toRemove The maximum amount to remove
     * @return The amount that was actually extracted
     */
    BigInteger extractEmc(@Nonnull ItemStack stack, BigInteger toRemove);

    /**
     * Gets the current amount of EMC in this IEMCStorage
     *
     * @return The current EMC stored
     */

    BigInteger getStoredEmc(@Nonnull ItemStack stack);

    /**
     * Gets the maximum EMC that is allowed to be stored in this stack
     *
     * @param stack The stack to query
     * @return The maximum amount of publicly-accessible EMC that can be stored in this stack
     */
    BigInteger getMaximumEmc(@Nonnull ItemStack stack);

    default BigInteger getNeededEmc(@Nonnull ItemStack stack) {
        return getMaximumEmc(stack).subtract(getStoredEmc(stack)).max(BigInteger.ZERO);
    }
}

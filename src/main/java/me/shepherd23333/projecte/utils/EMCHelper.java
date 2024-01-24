package me.shepherd23333.projecte.utils;

import me.shepherd23333.projecte.api.item.IItemEmc;
import me.shepherd23333.projecte.emc.EMCMapper;
import me.shepherd23333.projecte.emc.FuelMapper;
import me.shepherd23333.projecte.emc.SimpleStack;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class for EMC.
 * Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class EMCHelper {
    /**
     * Consumes EMC from fuel items or Klein Stars
     * Any extra EMC is discarded !!! To retain remainder EMC use ItemPE.consumeFuel()
     */
    public static BigInteger consumePlayerFuel(EntityPlayer player, BigInteger minFuel) {
        if (player.capabilities.isCreativeMode) {
            return minFuel;
        }

        IItemHandler inv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        Map<Integer, Integer> map = new LinkedHashMap<>();
        boolean metRequirement = false;
        BigInteger emcConsumed = BigInteger.ZERO;

        ItemStack offhand = player.getHeldItemOffhand();

        if (!offhand.isEmpty() && offhand.getItem() instanceof IItemEmc) {
            IItemEmc itemEmc = ((IItemEmc) offhand.getItem());
            if (itemEmc.getStoredEmc(offhand).compareTo(minFuel) >= 0) {
                itemEmc.extractEmc(offhand, minFuel);
                player.inventoryContainer.detectAndSendChanges();
                return minFuel;
            }
        }

        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack.isEmpty()) {
                continue;
            } else if (stack.getItem() instanceof IItemEmc) {
                IItemEmc itemEmc = ((IItemEmc) stack.getItem());
                if (itemEmc.getStoredEmc(stack).compareTo(minFuel) >= 0) {
                    itemEmc.extractEmc(stack, minFuel);
                    player.inventoryContainer.detectAndSendChanges();
                    return minFuel;
                }
            } else if (!metRequirement) {
                if (FuelMapper.isStackFuel(stack)) {
                    BigDecimal emc = new BigDecimal(getEmcValue(stack));
                    BigDecimal t = new BigDecimal(minFuel.subtract(emcConsumed));
                    int toRemove = t.divide(emc, RoundingMode.CEILING).intValueExact();

                    if (stack.getCount() >= toRemove) {
                        map.put(i, toRemove);
                        emcConsumed = emcConsumed.add(emc.multiply(BigDecimal.valueOf(toRemove)).toBigInteger());
                        metRequirement = true;
                    } else {
                        map.put(i, stack.getCount());
                        emcConsumed = emcConsumed.add(emc.multiply(BigDecimal.valueOf(stack.getCount())).toBigInteger());

                        if (emcConsumed.compareTo(minFuel) >= 0) {
                            metRequirement = true;
                        }
                    }

                }
            }
        }

        if (metRequirement) {
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                inv.extractItem(entry.getKey(), entry.getValue(), false);
            }

            player.inventoryContainer.detectAndSendChanges();
            return emcConsumed;
        }

        return BigInteger.ONE.negate();
    }

    public static boolean doesBlockHaveEmc(Block block) {
        return block != null && doesItemHaveEmc(new ItemStack(block));
    }

    public static boolean doesItemHaveEmc(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        SimpleStack iStack = new SimpleStack(stack);

        if (!iStack.isValid()) {
            return false;
        }

        if (ItemHelper.isDamageable(stack)) {
            iStack = iStack.withMeta(0);
        }

        return EMCMapper.mapContains(iStack);
    }

    public static boolean doesItemHaveEmc(Item item) {
        return item != null && doesItemHaveEmc(new ItemStack(item));
    }

    public static BigInteger getEmcValue(Block block) {
        SimpleStack stack = new SimpleStack(new ItemStack(block));

        if (stack.isValid() && EMCMapper.mapContains(stack)) {
            return EMCMapper.getEmcValue(stack);
        }

        return BigInteger.ZERO;
    }

    public static BigInteger getEmcValue(Item item) {
        SimpleStack stack = new SimpleStack(new ItemStack(item));

        if (stack.isValid() && EMCMapper.mapContains(stack)) {
            return EMCMapper.getEmcValue(stack);
        }

        return BigInteger.ZERO;
    }

    /**
     * Does not consider stack size
     */
    public static BigInteger getEmcValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return BigInteger.ZERO;
        }

        SimpleStack iStack = new SimpleStack(stack);

        if (!iStack.isValid()) {
            return BigInteger.ZERO;
        }

        if (!EMCMapper.mapContains(iStack) && ItemHelper.isDamageable(stack)) {
            //We don't have an emc value for id:metadata, so lets check if we have a value for id:0 and apply a damage multiplier based on that emc value.
            iStack = iStack.withMeta(0);

            if (EMCMapper.mapContains(iStack)) {
                BigInteger emc = EMCMapper.getEmcValue(iStack);

                // maxDmg + 1 because vanilla lets you use the tool one more time
                // when item damage == max damage (shows as Durability: 0 / max)
                int relDamage = (stack.getMaxDamage() + 1 - stack.getItemDamage());

                if (relDamage <= 0) {
                    // This may happen when mods overflow their max damage or item damage.
                    // Don't use durability or enchants for emc calculation if this happens.
                    return emc;
                }

                BigInteger result = emc.multiply(BigInteger.valueOf(relDamage));

                if (result.compareTo(BigInteger.ZERO) <= 0) {
                    //Congratulations, big number is big.
                    return emc;
                }

                result = result.divide(BigInteger.valueOf(stack.getMaxDamage()));
                boolean positive = result.compareTo(BigInteger.ZERO) > 0;
                result = result.add(getEnchantEmcBonus(stack));

                //If it was positive and then became negative that means it overflowed
                if (positive && result.compareTo(BigInteger.ZERO) < 0) {
                    return emc;
                }

                positive = result.compareTo(BigInteger.ZERO) > 0;
                result = result.add(getStoredEMCBonus(stack));

                //If it was positive and then became negative that means it overflowed
                if (positive && result.compareTo(BigInteger.ZERO) < 0) {
                    return emc;
                }

                if (result.compareTo(BigInteger.ZERO) <= 0) {
                    return BigInteger.ONE;
                }

                return result;
            }
        } else {
            if (EMCMapper.mapContains(iStack)) {
                return EMCMapper.getEmcValue(iStack).add(getEnchantEmcBonus(stack)).add(getStoredEMCBonus(stack));
            }
        }

        return BigInteger.ZERO;
    }

    private static BigInteger getEnchantEmcBonus(ItemStack stack) {
        BigInteger result = BigInteger.ZERO;

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);

        if (!enchants.isEmpty()) {
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                Enchantment ench = entry.getKey();
                if (ench == null || ench.getRarity().getWeight() == 0) {
                    continue;
                }

                result = result.add(BigInteger.valueOf(Constants.ENCH_EMC_BONUS / ench.getRarity().getWeight() * entry.getValue()));
            }
        }

        return result;
    }

    public static BigInteger getEmcSellValue(ItemStack stack) {
        BigInteger originalValue = EMCHelper.getEmcValue(stack);

        if (originalValue.equals(BigInteger.ZERO)) {
            return BigInteger.ZERO;
        }

        BigInteger emc = new BigDecimal(originalValue).multiply(BigDecimal.valueOf(EMCMapper.covalenceLoss))
                .setScale(0, RoundingMode.FLOOR).toBigInteger();

        if (emc.compareTo(BigInteger.ONE) < 0) {
            if (EMCMapper.covalenceLossRounding) {
                emc = BigInteger.ONE;
            } else {
                emc = BigInteger.ZERO;
            }
        }

        return emc;
    }

    public static String getEmcSellString(ItemStack stack, int stackSize) {
        if (EMCMapper.covalenceLoss == 1.0) {
            return " ";
        }

        BigInteger emc = EMCHelper.getEmcSellValue(stack);

        return " (" + EMCFormat.format(emc.multiply(BigInteger.valueOf(stackSize))) + ")";
    }

    public static BigInteger getKleinStarMaxEmc(ItemStack stack) {
        return Constants.MAX_KLEIN_EMC.multiply(BigInteger.valueOf(4).pow(stack.getItemDamage()));
    }

    private static BigInteger getStoredEMCBonus(ItemStack stack) {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("StoredEMC")) {
            return new BigInteger(stack.getTagCompound().getString("StoredEMC"));
        } else if (stack.getItem() instanceof IItemEmc) {
            return ((IItemEmc) stack.getItem()).getStoredEmc(stack);
        }
        return BigInteger.ZERO;
    }

    public static BigInteger getEMCPerDurability(ItemStack stack) {
        if (stack.isEmpty())
            return BigInteger.ZERO;

        if (ItemHelper.isItemRepairable(stack)) {
            ItemStack stackCopy = stack.copy();
            stackCopy.setItemDamage(0);
            BigInteger emc = new BigDecimal(EMCHelper.getEmcValue(stackCopy))
                    .divide(BigDecimal.valueOf(stack.getMaxDamage()), 0, RoundingMode.CEILING)
                    .toBigInteger();
            return emc.max(BigInteger.ONE);
        }
        return BigInteger.ONE;
    }

    /**
     * Adds the given amount to the amount of unprocessed EMC the stack has.
     * The amount returned should be used for figuring out how much EMC actually gets removed.
     * While the remaining fractional EMC will be stored in UnprocessedEMC.
     *
     * @param stack  The stack to set the UnprocessedEMC tag to.
     * @param amount The partial amount of EMC to add with the current UnprocessedEMC
     * @return The amount of non fractional EMC no longer being stored in UnprocessedEMC.
     */
    public static BigInteger removeFractionalEMC(ItemStack stack, double amount) {
        NBTTagCompound nbt = ItemHelper.getOrCreateCompound(stack);
        BigDecimal unprocessedEMC = nbt.hasKey("UnprocessedEMC") ? new BigDecimal(nbt.getString("UnprocessedEMC")) : BigDecimal.ZERO;
        unprocessedEMC = unprocessedEMC.add(BigDecimal.valueOf(amount));
        BigInteger toRemove = unprocessedEMC.toBigInteger();
        unprocessedEMC = unprocessedEMC.subtract(new BigDecimal(toRemove));
        stack.getTagCompound().setString("UnprocessedEMC", unprocessedEMC.toString());
        return toRemove;
    }
}

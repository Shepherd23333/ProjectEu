package me.shepherd23333.projecte.gameObjs.tiles;

import me.shepherd23333.projecte.api.item.IItemEmc;
import me.shepherd23333.projecte.api.tile.IEmcAcceptor;
import me.shepherd23333.projecte.api.tile.IEmcProvider;
import me.shepherd23333.projecte.gameObjs.container.slots.SlotPredicates;
import me.shepherd23333.projecte.utils.Constants;
import me.shepherd23333.projecte.utils.EMCHelper;
import me.shepherd23333.projecte.utils.ItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;

public class RelayMK1Tile extends TileEmc implements IEmcAcceptor, IEmcProvider {
    private final ItemStackHandler input;
    private final ItemStackHandler output = new StackHandler(1);
    private final IItemHandler automationInput;
    private final IItemHandler automationOutput = new WrappedItemHandler(output, WrappedItemHandler.WriteMode.IN_OUT) {
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return SlotPredicates.IITEMEMC.test(stack)
                    ? super.insertItem(slot, stack, simulate)
                    : stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack stack = getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IItemEmc) {
                IItemEmc item = ((IItemEmc) stack.getItem());
                if (item.getStoredEmc(stack).compareTo(item.getMaximumEmc(stack)) >= 0) {
                    return super.extractItem(slot, amount, simulate);
                } else {
                    return ItemStack.EMPTY;
                }
            }

            return super.extractItem(slot, amount, simulate);
        }
    };
    private final BigInteger chargeRate;
    private BigDecimal bonusEMC;

    public RelayMK1Tile() {
        this(7, Constants.RELAY_MK1_MAX, Constants.RELAY_MK1_OUTPUT);
    }

    RelayMK1Tile(int sizeInv, BigInteger maxEmc, BigInteger chargeRate) {
        super(maxEmc);
        this.chargeRate = chargeRate;
        input = new StackHandler(sizeInv) {
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return SlotPredicates.RELAY_INV.test(stack)
                        ? super.insertItem(slot, stack, simulate)
                        : stack;
            }
        };
        automationInput = new WrappedItemHandler(input, WrappedItemHandler.WriteMode.IN);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> cap, EnumFacing side) {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(cap, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> cap, EnumFacing side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == EnumFacing.DOWN) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(automationOutput);
            } else return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(automationInput);
        }
        return super.getCapability(cap, side);
    }

    private ItemStack getCharging() {
        return output.getStackInSlot(0);
    }

    private ItemStack getBurn() {
        return input.getStackInSlot(0);
    }

    public IItemHandler getInput() {
        return input;
    }

    public IItemHandler getOutput() {
        return output;
    }

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }

        sendEmc();
        ItemHelper.compactInventory(input);

        ItemStack stack = getBurn();

        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof IItemEmc) {
                IItemEmc itemEmc = ((IItemEmc) stack.getItem());
                BigInteger emcVal = itemEmc.getStoredEmc(stack);

                if (emcVal.compareTo(chargeRate) > 0) {
                    emcVal = chargeRate;
                }

                if (emcVal.compareTo(BigInteger.ZERO) > 0 && this.getStoredEmc().add(emcVal).compareTo(this.getMaximumEmc()) <= 0) {
                    this.addEMC(emcVal);
                    itemEmc.extractEmc(stack, emcVal);
                }
            } else {
                BigInteger emcVal = EMCHelper.getEmcSellValue(stack);

                if (emcVal.compareTo(BigInteger.ZERO) > 0 && this.getStoredEmc().add(emcVal).compareTo(this.getMaximumEmc()) <= 0) {
                    this.addEMC(emcVal);
                    getBurn().shrink(1);
                }
            }
        }

        ItemStack chargeable = getCharging();

        if (!chargeable.isEmpty() && this.getStoredEmc().compareTo(BigInteger.ZERO) > 0 && chargeable.getItem() instanceof IItemEmc) {
            chargeItem(chargeable);
        }
    }

    private void sendEmc() {
        if (this.getStoredEmc().equals(BigInteger.ZERO))
            return;

        if (this.getStoredEmc().compareTo(chargeRate) <= 0) {
            this.sendToAllAcceptors(this.getStoredEmc());
        } else {
            this.sendToAllAcceptors(chargeRate);
        }
    }

    private void chargeItem(ItemStack chargeable) {
        IItemEmc itemEmc = ((IItemEmc) chargeable.getItem());
        BigInteger starEmc = itemEmc.getStoredEmc(chargeable);
        BigInteger maxStarEmc = itemEmc.getMaximumEmc(chargeable);
        BigInteger toSend = this.getStoredEmc().compareTo(chargeRate) < 0 ? this.getStoredEmc() : chargeRate;

        if (starEmc.add(toSend).compareTo(maxStarEmc) <= 0) {
            itemEmc.addEmc(chargeable, toSend);
            this.removeEMC(toSend);
        } else {
            toSend = maxStarEmc.subtract(starEmc);
            itemEmc.addEmc(chargeable, toSend);
            this.removeEMC(toSend);
        }
    }

    public BigDecimal getItemChargeProportion() {
        if (!getCharging().isEmpty() && getCharging().getItem() instanceof IItemEmc) {
            return new BigDecimal(((IItemEmc) getCharging().getItem()).getStoredEmc(getCharging())).divide(new BigDecimal(((IItemEmc) getCharging().getItem()).getMaximumEmc(getCharging())));
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal getInputBurnProportion() {
        if (getBurn().isEmpty()) {
            return BigDecimal.ZERO;
        }

        if (getBurn().getItem() instanceof IItemEmc) {
            return new BigDecimal(((IItemEmc) getBurn().getItem()).getStoredEmc(getBurn())).divide(new BigDecimal(((IItemEmc) getBurn().getItem()).getMaximumEmc(getBurn())));
        }

        return new BigDecimal(getBurn().getCount()).divide(BigDecimal.valueOf(getBurn().getMaxStackSize()));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        input.deserializeNBT(nbt.getCompoundTag("Input"));
        output.deserializeNBT(nbt.getCompoundTag("Output"));
        bonusEMC = new BigDecimal(nbt.getString("BonusEMC"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setTag("Input", input.serializeNBT());
        nbt.setTag("Output", output.serializeNBT());
        nbt.setString("BonusEMC", bonusEMC.toString());
        return nbt;
    }

    @Override
    public BigInteger acceptEMC(@Nonnull EnumFacing side, BigInteger toAccept) {
        if (world.getTileEntity(pos.offset(side)) instanceof RelayMK1Tile) {
            return BigInteger.ZERO; // Do not accept from other relays - avoid infinite loop / thrashing
        } else {
            BigInteger toAdd = maximumEMC.subtract(currentEMC).min(toAccept);
            currentEMC = currentEMC.add(toAdd);
            return toAdd;
        }
    }

    public void addBonus(@Nonnull EnumFacing side, BigDecimal bonus) {
        if (world.getTileEntity(pos.offset(side)) instanceof RelayMK1Tile) {
            return; // Do not accept from other relays - avoid infinite loop / thrashing
        }
        bonusEMC = bonusEMC.add(bonus);
        if (bonusEMC.compareTo(BigDecimal.ONE) >= 0) {
            BigInteger extraEMC = bonusEMC.toBigInteger();
            bonusEMC = bonusEMC.subtract(new BigDecimal(extraEMC));
            currentEMC = currentEMC.add(maximumEMC.subtract(currentEMC).min(extraEMC));
        }
    }

    @Override
    public BigInteger provideEMC(@Nonnull EnumFacing side, BigInteger toExtract) {
        BigInteger toRemove = currentEMC.min(toExtract);
        currentEMC = currentEMC.subtract(toRemove);
        return toRemove;
    }
}

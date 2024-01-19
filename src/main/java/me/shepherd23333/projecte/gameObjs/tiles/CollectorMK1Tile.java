package me.shepherd23333.projecte.gameObjs.tiles;

import me.shepherd23333.projecte.api.item.IItemEmc;
import me.shepherd23333.projecte.api.tile.IEmcAcceptor;
import me.shepherd23333.projecte.api.tile.IEmcProvider;
import me.shepherd23333.projecte.emc.FuelMapper;
import me.shepherd23333.projecte.gameObjs.container.slots.SlotPredicates;
import me.shepherd23333.projecte.utils.Constants;
import me.shepherd23333.projecte.utils.EMCHelper;
import me.shepherd23333.projecte.utils.ItemHelper;
import me.shepherd23333.projecte.utils.WorldHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public class CollectorMK1Tile extends TileEmc implements IEmcProvider, IEmcAcceptor {
    private final ItemStackHandler input = new StackHandler(getInvSize());
    private final ItemStackHandler auxSlots = new StackHandler(3);
    private final CombinedInvWrapper toSort = new CombinedInvWrapper(new RangedWrapper(auxSlots, UPGRADING_SLOT, UPGRADING_SLOT + 1), input);
    private final IItemHandler automationInput = new WrappedItemHandler(input, WrappedItemHandler.WriteMode.IN) {
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return SlotPredicates.COLLECTOR_INV.test(stack)
                    ? super.insertItem(slot, stack, simulate)
                    : stack;
        }
    };
    private final IItemHandler automationAuxSlots = new WrappedItemHandler(auxSlots, WrappedItemHandler.WriteMode.OUT) {
        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int count, boolean simulate) {
            if (slot == UPGRADE_SLOT)
                return super.extractItem(slot, count, simulate);
            else return ItemStack.EMPTY;
        }
    };
    public static final int UPGRADING_SLOT = 0;
    public static final int UPGRADE_SLOT = 1;
    public static final int LOCK_SLOT = 2;

    private final BigInteger emcGen;
    private boolean hasChargeableItem;
    private boolean hasFuel;
    private BigInteger storedFuelEmc;
    private BigDecimal unprocessedEMC;

    public CollectorMK1Tile() {
        super(Constants.COLLECTOR_MK1_MAX);
        emcGen = Constants.COLLECTOR_MK1_GEN;
    }

    public CollectorMK1Tile(BigInteger maxEmc, BigInteger emcGen) {
        super(maxEmc);
        this.emcGen = emcGen;
    }

    public IItemHandler getInput() {
        return input;
    }

    public IItemHandler getAux() {
        return auxSlots;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> cap, EnumFacing side) {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(cap, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> cap, EnumFacing side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side != null && side.getAxis().isVertical()) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(automationAuxSlots);
            } else {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(automationInput);
            }
        }
        return super.getCapability(cap, side);
    }

    protected int getInvSize() {
        return 8;
    }

    private ItemStack getUpgraded() {
        return auxSlots.getStackInSlot(UPGRADE_SLOT);
    }

    private ItemStack getLock() {
        return auxSlots.getStackInSlot(LOCK_SLOT);
    }

    private ItemStack getUpgrading() {
        return auxSlots.getStackInSlot(UPGRADING_SLOT);
    }

    @Override
    public void update() {
        if (world.isRemote)
            return;

        ItemHelper.compactInventory(toSort);
        checkFuelOrKlein();
        updateEmc();
        rotateUpgraded();
    }

    private void rotateUpgraded() {
        if (!getUpgraded().isEmpty()) {
            if (getLock().isEmpty()
                    || getUpgraded().getItem() != getLock().getItem()
                    || getUpgraded().getCount() >= getUpgraded().getMaxStackSize()) {
                auxSlots.setStackInSlot(UPGRADE_SLOT, ItemHandlerHelper.insertItemStacked(input, getUpgraded().copy(), false));
            }
        }
    }

    private void checkFuelOrKlein() {
        if (!getUpgrading().isEmpty() && getUpgrading().getItem() instanceof IItemEmc) {
            IItemEmc itemEmc = ((IItemEmc) getUpgrading().getItem());
            if (itemEmc.getStoredEmc(getUpgrading()) != itemEmc.getMaximumEmc(getUpgrading())) {
                hasChargeableItem = true;
                hasFuel = false;
            } else {
                hasChargeableItem = false;
            }
        } else if (!getUpgrading().isEmpty()) {
            hasFuel = true;
            hasChargeableItem = false;
        } else {
            hasFuel = false;
            hasChargeableItem = false;
        }
    }

    private void updateEmc() {
        if (!this.hasMaxedEmc()) {
            unprocessedEMC = unprocessedEMC.add(new BigDecimal(emcGen).multiply(BigDecimal.valueOf(getSunLevel() / 320.0f)));
            if (unprocessedEMC.compareTo(BigDecimal.ONE) >= 0) {
                BigInteger emcToAdd = unprocessedEMC.toBigInteger();
                this.addEMC(emcToAdd);
                unprocessedEMC = unprocessedEMC.subtract(new BigDecimal(emcToAdd));
            }
        }

        if (this.getStoredEmc().equals(BigInteger.ZERO)) {
            return;
        } else if (hasChargeableItem) {
            BigInteger toSend = this.getStoredEmc().compareTo(emcGen) < 0 ? this.getStoredEmc() : emcGen;
            IItemEmc item = (IItemEmc) getUpgrading().getItem();

            BigInteger itemEmc = item.getStoredEmc(getUpgrading());
            BigInteger maxItemEmc = item.getMaximumEmc(getUpgrading());

            if ((itemEmc.add(toSend)).compareTo(maxItemEmc) > 0) {
                toSend = maxItemEmc.subtract(itemEmc);
            }

            item.addEmc(getUpgrading(), toSend);
            this.removeEMC(toSend);
        } else if (hasFuel) {
            if (FuelMapper.getFuelUpgrade(getUpgrading()).isEmpty()) {
                auxSlots.setStackInSlot(UPGRADING_SLOT, ItemStack.EMPTY);
            }

            ItemStack result = getLock().isEmpty() ? FuelMapper.getFuelUpgrade(getUpgrading()) : getLock().copy();

            BigInteger upgradeCost = EMCHelper.getEmcValue(result).subtract(EMCHelper.getEmcValue(getUpgrading()));

            if (upgradeCost.compareTo(BigInteger.ZERO) >= 0 && this.getStoredEmc().compareTo(upgradeCost) >= 0) {
                ItemStack upgrade = getUpgraded();

                if (getUpgraded().isEmpty()) {
                    this.removeEMC(upgradeCost);
                    auxSlots.setStackInSlot(UPGRADE_SLOT, result);
                    getUpgrading().shrink(1);
                } else if (ItemHelper.basicAreStacksEqual(result, upgrade) && upgrade.getCount() < upgrade.getMaxStackSize()) {
                    this.removeEMC(upgradeCost);
                    getUpgraded().grow(1);
                    getUpgrading().shrink(1);
                }
            }
        } else {
            //Only send EMC when we are not upgrading fuel or charging an item
            BigInteger toSend = this.getStoredEmc().compareTo(emcGen) < 0 ? this.getStoredEmc() : emcGen;
            this.sendToAllAcceptors(toSend);
            this.sendRelayBonus();
        }
    }

    public BigInteger getEmcToNextGoal() {
        if (!getLock().isEmpty()) {
            return EMCHelper.getEmcValue(getLock()).subtract(EMCHelper.getEmcValue(getUpgrading()));
        } else {
            return EMCHelper.getEmcValue(FuelMapper.getFuelUpgrade(getUpgrading())).subtract(EMCHelper.getEmcValue(getUpgrading()));
        }
    }

    public BigInteger getItemCharge() {
        if (!getUpgrading().isEmpty() && getUpgrading().getItem() instanceof IItemEmc) {
            return ((IItemEmc) getUpgrading().getItem()).getStoredEmc(getUpgrading());
        }

        return BigInteger.ONE.negate();
    }

    public double getItemChargeProportion() {
        BigInteger charge = getItemCharge();

        if (getUpgrading().isEmpty() || charge.compareTo(BigInteger.ZERO) <= 0 || !(getUpgrading().getItem() instanceof IItemEmc)) {
            return -1;
        }

        BigInteger max = ((IItemEmc) getUpgrading().getItem()).getMaximumEmc(getUpgrading());
        if (charge.compareTo(max) >= 0) {
            return 1;
        }

        return new BigDecimal(charge).divide(new BigDecimal(max)).doubleValue();
    }

    public int getSunLevel() {
        if (world.provider.doesWaterVaporize()) {
            return 16;
        }
        return world.getLight(getPos().up()) + 1;
    }

    public double getFuelProgress() {
        if (getUpgrading().isEmpty() || !FuelMapper.isStackFuel(getUpgrading())) {
            return 0;
        }

        BigInteger reqEmc;

        if (!getLock().isEmpty()) {
            reqEmc = EMCHelper.getEmcValue(getLock()).subtract(EMCHelper.getEmcValue(getUpgrading()));

            if (reqEmc.compareTo(BigInteger.ZERO) < 0) {
                return 0;
            }
        } else {
            if (FuelMapper.getFuelUpgrade(getUpgrading()).isEmpty()) {
                auxSlots.setStackInSlot(UPGRADING_SLOT, ItemStack.EMPTY);
                return 0;
            } else {
                reqEmc = EMCHelper.getEmcValue(FuelMapper.getFuelUpgrade(getUpgrading())).subtract(EMCHelper.getEmcValue(getUpgrading()));
            }

        }

        if (getStoredEmc().compareTo(reqEmc) >= 0) {
            return 1;
        }

        return new BigDecimal(getStoredEmc()).divide(new BigDecimal(reqEmc)).doubleValue();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        storedFuelEmc = new BigInteger(nbt.getString("FuelEMC"));
        input.deserializeNBT(nbt.getCompoundTag("Input"));
        auxSlots.deserializeNBT(nbt.getCompoundTag("AuxSlots"));
        unprocessedEMC = new BigDecimal(nbt.getString("UnprocessedEMC"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setString("FuelEMC", storedFuelEmc.toString());
        nbt.setTag("Input", input.serializeNBT());
        nbt.setTag("AuxSlots", auxSlots.serializeNBT());
        nbt.setString("UnprocessedEMC", unprocessedEMC.toPlainString());
        return nbt;
    }

    private void sendRelayBonus() {
        for (Map.Entry<EnumFacing, TileEntity> entry : WorldHelper.getAdjacentTileEntitiesMapped(world, this).entrySet()) {
            EnumFacing dir = entry.getKey();
            TileEntity tile = entry.getValue();

            if (tile instanceof RelayMK3Tile) {
                ((RelayMK3Tile) tile).addBonus(dir, BigDecimal.valueOf(0.5));
            } else if (tile instanceof RelayMK2Tile) {
                ((RelayMK2Tile) tile).addBonus(dir, BigDecimal.valueOf(0.15));
            } else if (tile instanceof RelayMK1Tile) {
                ((RelayMK1Tile) tile).addBonus(dir, BigDecimal.valueOf(0.05));
            }
        }
    }

    @Override
    public BigInteger provideEMC(@Nonnull EnumFacing side, BigInteger toExtract) {
        BigInteger toRemove = currentEMC.min(toExtract);
        removeEMC(toRemove);
        return toRemove;
    }

    @Override
    public BigInteger acceptEMC(@Nonnull EnumFacing side, BigInteger toAccept) {
        if (hasFuel || hasChargeableItem) {
            //Collector accepts EMC from providers if it has fuel/chargeable. Otherwise it sends it to providers
            BigInteger toAdd = maximumEMC.subtract(currentEMC).min(toAccept);
            currentEMC = currentEMC.add(toAdd);
            return toAdd;
        }
        return BigInteger.ZERO;
    }
}

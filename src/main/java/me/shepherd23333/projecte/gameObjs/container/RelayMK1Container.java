package me.shepherd23333.projecte.gameObjs.container;

import me.shepherd23333.projecte.gameObjs.ObjHandler;
import me.shepherd23333.projecte.gameObjs.container.slots.SlotPredicates;
import me.shepherd23333.projecte.gameObjs.container.slots.ValidatedSlot;
import me.shepherd23333.projecte.gameObjs.tiles.RelayMK1Tile;
import me.shepherd23333.projecte.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;

public class RelayMK1Container extends BigIntegerContainer {
    final RelayMK1Tile tile;
    public BigDecimal kleinChargeProgress = BigDecimal.ZERO;
    public BigDecimal inputBurnProgress = BigDecimal.ZERO;
    public BigInteger emc = BigInteger.ZERO;

    public RelayMK1Container(InventoryPlayer invPlayer, RelayMK1Tile relay) {
        this.tile = relay;
        initSlots(invPlayer);
    }

    void initSlots(InventoryPlayer invPlayer) {
        IItemHandler input = tile.getInput();
        IItemHandler output = tile.getOutput();

        //Klein Star charge slot
        this.addSlotToContainer(new ValidatedSlot(input, 0, 67, 43, SlotPredicates.RELAY_INV));

        int counter = input.getSlots() - 1;
        //Main Relay inventory
        for (int i = 0; i <= 1; i++)
            for (int j = 0; j <= 2; j++)
                this.addSlotToContainer(new ValidatedSlot(input, counter--, 27 + i * 18, 17 + j * 18, SlotPredicates.RELAY_INV));

        //Burning slot
        this.addSlotToContainer(new ValidatedSlot(output, 0, 127, 43, SlotPredicates.IITEMEMC));

        //Player Inventory
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 95 + i * 18));

        //Player Hotbar
        for (int i = 0; i < 9; i++)
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 153));
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        PacketHandler.sendProgressBarUpdateBigInteger(listener, this, 0, tile.getStoredEmc());
        PacketHandler.sendProgressBarUpdateInt(listener, this, 1, tile.getItemChargeProportion().multiply(BigDecimal.valueOf(8000)).intValue());
        PacketHandler.sendProgressBarUpdateInt(listener, this, 2, tile.getInputBurnProportion().multiply(BigDecimal.valueOf(8000)).intValue());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (emc != tile.getStoredEmc()) {
            for (IContainerListener icrafting : this.listeners) {
                PacketHandler.sendProgressBarUpdateBigInteger(icrafting, this, 0, tile.getStoredEmc());
            }

            emc = tile.getStoredEmc();
        }

        if (kleinChargeProgress != tile.getItemChargeProportion()) {
            for (IContainerListener icrafting : this.listeners) {
                PacketHandler.sendProgressBarUpdateInt(icrafting, this, 1, tile.getItemChargeProportion().multiply(BigDecimal.valueOf(8000)).intValue());
            }

            kleinChargeProgress = tile.getItemChargeProportion();
        }

        if (inputBurnProgress != tile.getInputBurnProportion()) {
            for (IContainerListener icrafting : this.listeners) {
                PacketHandler.sendProgressBarUpdateInt(icrafting, this, 2, tile.getInputBurnProportion().multiply(BigDecimal.valueOf(8000)).intValue());
            }

            inputBurnProgress = tile.getInputBurnProportion();
        }

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0:
                emc = BigInteger.valueOf(data);
                break;
            case 1:
                kleinChargeProgress = BigDecimal.valueOf(data / 8000.0);
                break;
            case 2:
                inputBurnProgress = BigDecimal.valueOf(data / 8000.0);
                break;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBarBigInteger(int id, BigInteger data) {
        switch (id) {
            case 0:
                emc = data;
                break;
            default:
                updateProgressBar(id, data.intValueExact());
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        Slot slot = this.getSlot(slotIndex);

        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack newStack = stack.copy();

        if (slotIndex < 8) {
            if (!this.mergeItemStack(stack, 8, this.inventorySlots.size(), true))
                return ItemStack.EMPTY;
            slot.onSlotChanged();
        } else if (!this.mergeItemStack(stack, 0, 7, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        return slot.onTake(player, newStack);
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer player) {
        return player.world.getBlockState(tile.getPos()).getBlock() == ObjHandler.relay
                && player.getDistanceSq(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5) <= 64.0;
    }
}

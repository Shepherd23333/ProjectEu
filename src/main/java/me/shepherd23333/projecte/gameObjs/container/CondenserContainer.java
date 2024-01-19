package me.shepherd23333.projecte.gameObjs.container;

import me.shepherd23333.projecte.api.event.PlayerAttemptCondenserSetEvent;
import me.shepherd23333.projecte.gameObjs.blocks.Condenser;
import me.shepherd23333.projecte.gameObjs.container.slots.SlotCondenserLock;
import me.shepherd23333.projecte.gameObjs.container.slots.SlotPredicates;
import me.shepherd23333.projecte.gameObjs.container.slots.ValidatedSlot;
import me.shepherd23333.projecte.gameObjs.tiles.CondenserTile;
import me.shepherd23333.projecte.network.PacketHandler;
import me.shepherd23333.projecte.utils.Constants;
import me.shepherd23333.projecte.utils.EMCHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;

public class CondenserContainer extends BigIntegerContainer {
    protected final CondenserTile tile;
    public BigInteger displayEmc;
    public BigInteger requiredEmc;

    public CondenserContainer(InventoryPlayer invPlayer, CondenserTile condenser) {
        tile = condenser;
        tile.numPlayersUsing++;
        initSlots(invPlayer);
    }

    protected void initSlots(InventoryPlayer invPlayer) {
        this.addSlotToContainer(new SlotCondenserLock(tile.getLock(), 0, 12, 6));

        IItemHandler handler = tile.getInput();

        int counter = 0;
        //Condenser Inventory
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 13; j++)
                this.addSlotToContainer(new ValidatedSlot(handler, counter++, 12 + j * 18, 26 + i * 18, s -> SlotPredicates.HAS_EMC.test(s) && !tile.isStackEqualToLock(s)));

        //Player Inventory
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 48 + j * 18, 154 + i * 18));

        //Player Hotbar
        for (int i = 0; i < 9; i++)
            this.addSlotToContainer(new Slot(invPlayer, i, 48 + i * 18, 212));
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        PacketHandler.sendProgressBarUpdateBigInteger(listener, this, 0, tile.displayEmc);
        PacketHandler.sendProgressBarUpdateBigInteger(listener, this, 1, tile.requiredEmc);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (!displayEmc.equals(tile.displayEmc)) {
            for (IContainerListener listener : listeners) {
                PacketHandler.sendProgressBarUpdateBigInteger(listener, this, 0, tile.displayEmc);
            }

            displayEmc = tile.displayEmc;
        }

        if (requiredEmc != tile.requiredEmc) {
            for (IContainerListener listener : listeners) {
                PacketHandler.sendProgressBarUpdateBigInteger(listener, this, 1, tile.requiredEmc);
            }

            requiredEmc = tile.requiredEmc;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0:
                displayEmc = BigInteger.valueOf(data);
                break;
            case 1:
                requiredEmc = BigInteger.valueOf(data);
                break;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBarBigInteger(int id, BigInteger data) {
        switch (id) {
            case 0:
                displayEmc = data;
                break;
            case 1:
                requiredEmc = data;
                break;
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

        if (slotIndex <= 91) {
            if (!this.mergeItemStack(stack, 92, 127, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!EMCHelper.doesItemHaveEmc(stack) || !this.mergeItemStack(stack, 1, 91, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else slot.onSlotChanged();
        return slot.onTake(player, stack);
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer player) {
        return player.world.getBlockState(tile.getPos()).getBlock() instanceof Condenser
                && player.getDistanceSq(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        tile.numPlayersUsing--;
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slot, int button, ClickType flag, EntityPlayer player) {
        if (slot == 0 && (!tile.getLock().getStackInSlot(0).isEmpty() || MinecraftForge.EVENT_BUS.post(new PlayerAttemptCondenserSetEvent(player, player.inventory.getItemStack())))) {
            if (!player.getEntityWorld().isRemote) {
                tile.getLock().setStackInSlot(0, ItemStack.EMPTY);
                this.detectAndSendChanges();
            }

            return ItemStack.EMPTY;
        } else return super.slotClick(slot, button, flag, player);
    }

    public int getProgressScaled() {
        if (requiredEmc.equals(BigInteger.ZERO)) {
            return 0;
        }

        if (displayEmc.compareTo(requiredEmc) >= 0) {
            return Constants.MAX_CONDENSER_PROGRESS;
        }

        return new BigDecimal(displayEmc).divide(new BigDecimal(requiredEmc))
                .multiply(BigDecimal.valueOf(Constants.MAX_CONDENSER_PROGRESS)).intValueExact();
    }
}

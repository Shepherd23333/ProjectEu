package me.shepherd23333.projecte.gameObjs.container.slots.transmutation;

import me.shepherd23333.projecte.api.item.IItemEmc;
import me.shepherd23333.projecte.gameObjs.container.inventory.TransmutationInventory;
import me.shepherd23333.projecte.gameObjs.container.slots.SlotPredicates;
import me.shepherd23333.projecte.utils.EMCHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public class SlotInput extends SlotItemHandler {
    private final TransmutationInventory inv;

    public SlotInput(TransmutationInventory inv, int par2, int par3, int par4) {
        super(inv, par2, par3, par4);
        this.inv = inv;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return SlotPredicates.RELAY_INV.test(stack);
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int amount) {
        ItemStack stack = super.decrStackSize(amount);
        //Decrease the size of the stack
        if (stack.getItem() instanceof IItemEmc) {
            //If it was an EMC storing item then check for updates,
            // so that the right hand side shows the proper items
            inv.checkForUpdates();
        }
        return stack;
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        super.putStack(stack);

        if (stack.getItem() instanceof IItemEmc) {
            IItemEmc itemEmc = ((IItemEmc) stack.getItem());
            BigInteger remainingEmc = itemEmc.getMaximumEmc(stack).subtract(itemEmc.getStoredEmc(stack));
            BigInteger availableEMC = inv.getAvailableEMC();

            if (availableEMC.compareTo(remainingEmc) >= 0) {
                itemEmc.addEmc(stack, remainingEmc);
                inv.removeEmc(remainingEmc);
            } else {
                itemEmc.addEmc(stack, availableEMC);
                inv.removeEmc(availableEMC);
            }
        }

        if (EMCHelper.doesItemHaveEmc(stack)) {
            inv.handleKnowledge(stack.copy());
        }
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}

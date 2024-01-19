package me.shepherd23333.projecte.gameObjs.container.slots.transmutation;

import me.shepherd23333.projecte.api.item.IItemEmc;
import me.shepherd23333.projecte.gameObjs.container.inventory.TransmutationInventory;
import me.shepherd23333.projecte.gameObjs.container.slots.SlotPredicates;
import me.shepherd23333.projecte.utils.EMCHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public class SlotLock extends SlotItemHandler {
    private final TransmutationInventory inv;

    public SlotLock(TransmutationInventory inv, int par2, int par3, int par4) {
        super(inv, par2, par3, par4);
        this.inv = inv;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return SlotPredicates.RELAY_INV.test(stack);
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        super.putStack(stack);

        if (stack.getItem() instanceof IItemEmc) {
            IItemEmc itemEmc = ((IItemEmc) stack.getItem());
            BigInteger storedEmc = itemEmc.getStoredEmc(stack);
            inv.addEmc(storedEmc);
            itemEmc.extractEmc(stack, storedEmc);
        }

        if (EMCHelper.doesItemHaveEmc(stack)) {
            inv.handleKnowledge(stack.copy());
        }
    }

    @Nonnull
    @Override
    public ItemStack onTake(EntityPlayer player, @Nonnull ItemStack stack) {
        stack = super.onTake(player, stack);
        inv.updateClientTargets();
        return stack;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}

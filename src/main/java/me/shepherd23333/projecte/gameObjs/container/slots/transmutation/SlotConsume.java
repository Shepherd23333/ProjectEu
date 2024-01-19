package me.shepherd23333.projecte.gameObjs.container.slots.transmutation;

import me.shepherd23333.projecte.gameObjs.ObjHandler;
import me.shepherd23333.projecte.gameObjs.container.inventory.TransmutationInventory;
import me.shepherd23333.projecte.utils.EMCHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public class SlotConsume extends SlotItemHandler {
    private final TransmutationInventory inv;

    public SlotConsume(TransmutationInventory inv, int par2, int par3, int par4) {
        super(inv, par2, par3, par4);
        this.inv = inv;
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        inv.addEmc(EMCHelper.getEmcSellValue(stack).multiply(BigInteger.valueOf(stack.getCount())));
        this.onSlotChanged();
        inv.handleKnowledge(stack.copy());
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return EMCHelper.doesItemHaveEmc(stack) || stack.getItem() == ObjHandler.tome;
    }
}

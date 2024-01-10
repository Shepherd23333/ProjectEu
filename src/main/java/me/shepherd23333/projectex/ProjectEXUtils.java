package me.shepherd23333.projectex;

import me.shepherd23333.projecte.api.capabilities.IKnowledgeProvider;
import me.shepherd23333.projecte.api.event.PlayerAttemptLearnEvent;
import me.shepherd23333.projecte.utils.NBTWhitelist;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * @author LatvianModder
 */
public class ProjectEXUtils {
    public static ItemStack fixOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack1 = ItemHandlerHelper.copyStackWithSize(stack, 1);

        if (!stack1.getHasSubtypes() && stack1.isItemStackDamageable()) {
            stack1.setItemDamage(0);
        }

        if (!NBTWhitelist.shouldDupeWithNBT(stack1)) {
            stack1.setTagCompound(null);

            try {
                stack1.getItem().readNBTShareTag(stack1, null);
            } catch (Exception ex) {
                ex.printStackTrace();
                //Catch exception if readNBTShareTag fails
            }
        }

        return stack1;
    }

    public static int addKnowledge(EntityPlayer player, IKnowledgeProvider knowledgeProvider, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        if (!knowledgeProvider.hasKnowledge(stack)) {
            if (MinecraftForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(player, stack))) {
                return 0;
            }

            knowledgeProvider.addKnowledge(stack);
            return 2;
        }

        return 1;
    }
}
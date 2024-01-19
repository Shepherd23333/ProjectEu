package me.shepherd23333.projecte.gameObjs.container.inventory;

import me.shepherd23333.projecte.api.ProjectEAPI;
import me.shepherd23333.projecte.api.capabilities.IKnowledgeProvider;
import me.shepherd23333.projecte.api.event.PlayerAttemptLearnEvent;
import me.shepherd23333.projecte.api.item.IItemEmc;
import me.shepherd23333.projecte.emc.FuelMapper;
import me.shepherd23333.projecte.utils.EMCHelper;
import me.shepherd23333.projecte.utils.ItemHelper;
import me.shepherd23333.projecte.utils.NBTWhitelist;
import me.shepherd23333.projecte.utils.PlayerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.math.BigInteger;
import java.util.*;

public class TransmutationInventory extends CombinedInvWrapper {
    public final EntityPlayer player;
    public final IKnowledgeProvider provider;
    private final IItemHandlerModifiable inputLocks;
    private final IItemHandlerModifiable learning;
    public final IItemHandlerModifiable outputs;

    private static final int LOCK_INDEX = 8;
    private static final int FUEL_START = 12;
    public int learnFlag = 0;
    public int unlearnFlag = 0;
    public String filter = "";
    public int searchpage = 0;
    public final List<ItemStack> knowledge = new ArrayList<>();

    public TransmutationInventory(EntityPlayer player) {
        super((IItemHandlerModifiable) player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null).getInputAndLocks(),
                new ItemStackHandler(2), new ItemStackHandler(16));

        this.player = player;
        this.provider = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);

        this.inputLocks = itemHandler[0];
        this.learning = itemHandler[1];
        this.outputs = itemHandler[2];

        if (player.getEntityWorld().isRemote) {
            updateClientTargets();
        }
    }

    public void handleKnowledge(ItemStack stack) {
        if (stack.getCount() > 1) {
            stack.setCount(1);
        }

        if (ItemHelper.isDamageable(stack)) {
            stack.setItemDamage(0);
        }

        if (!provider.hasKnowledge(stack)) {
            if (stack.hasTagCompound() && !NBTWhitelist.shouldDupeWithNBT(stack)) {
                stack.setTagCompound(null);
            }

            if (!MinecraftForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(player, stack))) //Only show the "learned" text if the knowledge was added
            {
                learnFlag = 300;
                unlearnFlag = 0;
                provider.addKnowledge(stack);
            }

            if (!player.getEntityWorld().isRemote) {
                provider.sync(((EntityPlayerMP) player));
            }
        }

        updateClientTargets();
    }

    public void handleUnlearn(ItemStack stack) {
        if (stack.getCount() > 1) {
            stack.setCount(1);
        }

        if (ItemHelper.isDamageable(stack)) {
            stack.setItemDamage(0);
        }

        if (provider.hasKnowledge(stack)) {
            unlearnFlag = 300;
            learnFlag = 0;

            if (stack.hasTagCompound() && !NBTWhitelist.shouldDupeWithNBT(stack)) {
                stack.setTagCompound(null);
            }

            provider.removeKnowledge(stack);

            if (!player.getEntityWorld().isRemote) {
                provider.sync(((EntityPlayerMP) player));
            }
        }

        updateClientTargets();
    }

    public void checkForUpdates() {
        BigInteger matterEmc = EMCHelper.getEmcValue(outputs.getStackInSlot(0));
        BigInteger fuelEmc = EMCHelper.getEmcValue(outputs.getStackInSlot(FUEL_START));

        if (matterEmc.max(fuelEmc).compareTo(getAvailableEMC()) > 0) {
            updateClientTargets();
        }
    }

    public void updateClientTargets() {
        if (!this.player.getEntityWorld().isRemote) {
            return;
        }

        knowledge.clear();
        knowledge.addAll(provider.getKnowledge());

        for (int i = 0; i < outputs.getSlots(); i++) {
            outputs.setStackInSlot(i, ItemStack.EMPTY);
        }

        ItemStack lockCopy = ItemStack.EMPTY;

        knowledge.sort(Collections.reverseOrder(Comparator.comparing(EMCHelper::getEmcValue)));
        if (!inputLocks.getStackInSlot(LOCK_INDEX).isEmpty()) {
            lockCopy = ItemHelper.getNormalizedStack(inputLocks.getStackInSlot(LOCK_INDEX));

            if (ItemHelper.isDamageable(lockCopy)) {
                lockCopy.setItemDamage(0);
            }

            BigInteger reqEmc = EMCHelper.getEmcValue(inputLocks.getStackInSlot(LOCK_INDEX));

            if (getAvailableEMC().compareTo(reqEmc) < 0) {
                return;
            }

            if (lockCopy.hasTagCompound() && !NBTWhitelist.shouldDupeWithNBT(lockCopy)) {
                lockCopy.setTagCompound(null);
            }

            Iterator<ItemStack> iter = knowledge.iterator();
            int pagecounter = 0;

            while (iter.hasNext()) {
                ItemStack stack = iter.next();

                if (getAvailableEMC().compareTo(EMCHelper.getEmcValue(stack)) < 0) {
                    iter.remove();
                    continue;
                }

                if (ItemHelper.basicAreStacksEqual(lockCopy, stack)) {
                    iter.remove();
                    continue;
                }

                if (!doesItemMatchFilter(stack)) {
                    iter.remove();
                    continue;
                }

                if (pagecounter < (searchpage * 12)) {
                    pagecounter++;
                    iter.remove();
                }
            }
        } else {
            Iterator<ItemStack> iter = knowledge.iterator();
            int pagecounter = 0;

            while (iter.hasNext()) {
                ItemStack stack = iter.next();

                if (getAvailableEMC().compareTo(EMCHelper.getEmcValue(stack)) < 0) {
                    iter.remove();
                    continue;
                }

                if (!doesItemMatchFilter(stack)) {
                    iter.remove();
                    continue;
                }

                if (pagecounter < (searchpage * 12)) {
                    pagecounter++;
                    iter.remove();
                }
            }
        }

        int matterCounter = 0;
        int fuelCounter = 0;

        if (!lockCopy.isEmpty() && provider.hasKnowledge(lockCopy)) {
            if (FuelMapper.isStackFuel(lockCopy)) {
                outputs.setStackInSlot(FUEL_START, lockCopy);
                fuelCounter++;
            } else {
                outputs.setStackInSlot(0, lockCopy);
                matterCounter++;
            }
        }

        for (ItemStack stack : knowledge) {
            if (FuelMapper.isStackFuel(stack)) {
                if (fuelCounter < 4) {
                    outputs.setStackInSlot(FUEL_START + fuelCounter, stack);

                    fuelCounter++;
                }
            } else {
                if (matterCounter < 12) {
                    outputs.setStackInSlot(matterCounter, stack);

                    matterCounter++;
                }
            }
        }
    }

    private boolean doesItemMatchFilter(ItemStack stack) {
        String displayName;

        try {
            displayName = stack.getDisplayName().toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            e.printStackTrace();
            //From old code... Not sure if intended to not remove items that crash on getDisplayName
            return true;
        }

        if (displayName == null) {
            return false;
        } else if (filter.length() > 0 && !displayName.contains(filter)) {
            return false;
        }
        return true;
    }

    public void writeIntoOutputSlot(int slot, ItemStack item) {

        if (EMCHelper.doesItemHaveEmc(item)
                && EMCHelper.getEmcValue(item).compareTo(getAvailableEMC()) <= 0
                && provider.hasKnowledge(item)) {
            outputs.setStackInSlot(slot, item);
        } else {
            outputs.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    public void addEmc(BigInteger value) {
        int comp = value.compareTo(BigInteger.ZERO);
        if (comp == 0) {
            //Optimization to not look at the items if nothing will happen anyways
            return;
        }
        if (comp < 0) {
            //Make sure it is using the correct method so that it handles the klein stars properly
            removeEmc(value.negate());
        }
        //Start by trying to add it to the EMC items on the left
        for (int i = 0; i < inputLocks.getSlots(); i++) {
            if (i == LOCK_INDEX) {
                continue;
            }
            ItemStack stack = inputLocks.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IItemEmc) {
                IItemEmc itemEmc = ((IItemEmc) stack.getItem());
                BigInteger neededEmc = itemEmc.getMaximumEmc(stack).subtract(itemEmc.getStoredEmc(stack));
                if (value.compareTo(neededEmc) <= 0) {
                    //This item can store all of the amount being added
                    itemEmc.addEmc(stack, value);
                    return;
                }
                //else more than this item can fit, so fill the item and then continue going
                itemEmc.addEmc(stack, neededEmc);
                value = value.subtract(neededEmc);
            }
        }

        provider.setEmc(provider.getEmc().add(value));

        if (provider.getEmc().compareTo(BigInteger.ZERO) < 0) {
            provider.setEmc(BigInteger.ZERO);
        }

        if (!player.getEntityWorld().isRemote) {
            PlayerHelper.updateScore((EntityPlayerMP) player, PlayerHelper.SCOREBOARD_EMC, provider.getEmc());
        }
    }

    public void removeEmc(BigInteger value) {
        int comp = value.compareTo(BigInteger.ZERO);
        if (comp == 0) {
            //Optimization to not look at the items if nothing will happen anyways
            return;
        }
        if (comp < 0) {
            //Make sure it is using the correct method so that it handles the klein stars properly
            addEmc(value.negate());
        }
        if (value.compareTo(provider.getEmc()) > 0) {
            //Remove from provider first
            //This code runs first to simplify the logic
            //But it simulates removal first by extracting the amount from value and then removing that excess from items
            BigInteger toRemove = value.subtract(provider.getEmc());
            value = provider.getEmc();
            for (int i = 0; i < inputLocks.getSlots(); i++) {
                if (i == LOCK_INDEX) {
                    continue;
                }
                ItemStack stack = inputLocks.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof IItemEmc) {
                    IItemEmc itemEmc = ((IItemEmc) stack.getItem());
                    BigInteger storedEmc = itemEmc.getStoredEmc(stack);
                    if (toRemove.compareTo(storedEmc) <= 0) {
                        //The EMC that is being removed that the provider does not contain is satisfied by this IItemEMC
                        //Remove it and then
                        itemEmc.extractEmc(stack, toRemove);
                        break;
                    }
                    //Removes all the emc from this item
                    itemEmc.extractEmc(stack, storedEmc);
                    toRemove = toRemove.subtract(storedEmc);
                }
            }
        }
        provider.setEmc(provider.getEmc().subtract(value));

        if (provider.getEmc().compareTo(BigInteger.ZERO) < 0) {
            provider.setEmc(BigInteger.ZERO);
        }

        if (!player.getEntityWorld().isRemote) {
            PlayerHelper.updateScore((EntityPlayerMP) player, PlayerHelper.SCOREBOARD_EMC, provider.getEmc());
        }
    }

    public IItemHandlerModifiable getHandlerForSlot(int slot) {
        return super.getHandlerFromIndex(super.getIndexForSlot(slot));
    }

    public int getIndexFromSlot(int slot) {
        for (IItemHandlerModifiable h : itemHandler) {
            if (slot >= h.getSlots()) {
                slot -= h.getSlots();
            }
        }

        return slot;
    }

    /**
     * @return EMC available from the Provider + any klein stars in the input slots.
     */
    public BigInteger getAvailableEMC() {
        //TODO: Cache this value somehow, or at least cache which slots have IItemEMC in them?

        BigInteger emc = provider.getEmc();
        for (int i = 0; i < inputLocks.getSlots(); i++) {
            if (i == LOCK_INDEX) {
                //Skip it even though this technically could add to available EMC.
                //This is because this case can only happen if the provider is already at max EMC
                continue;
            }
            ItemStack stack = inputLocks.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IItemEmc) {
                IItemEmc itemEmc = ((IItemEmc) stack.getItem());
                BigInteger storedEmc = itemEmc.getStoredEmc(stack);
                emc = emc.add(storedEmc);
            }
        }
        return emc;
    }

}

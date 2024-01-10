package me.shepherd23333.projecte.playerData;

import me.shepherd23333.projecte.PECore;
import me.shepherd23333.projecte.emc.EMCMapper;
import me.shepherd23333.projecte.emc.SimpleStack;
import me.shepherd23333.projecte.utils.EMCHelper;
import me.shepherd23333.projecte.utils.ItemHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Transmutation {
    private static final List<ItemStack> CACHED_TOME_KNOWLEDGE = new ArrayList<>();

    public static void clearCache() {
        CACHED_TOME_KNOWLEDGE.clear();
    }

    public static void cacheFullKnowledge() {
        clearCache();
        for (SimpleStack stack : EMCMapper.emc.keySet()) {
            if (!stack.isValid()) {
                continue;
            }

            try {
                ItemStack s = stack.toItemStack();
                s.setCount(1);

                //Apparently items can still not have EMC if they are in the EMC map.
                if (EMCHelper.doesItemHaveEmc(s) && EMCHelper.getEmcValue(s) > 0 && !ItemHelper.containsItemStack(CACHED_TOME_KNOWLEDGE, s)) {
                    CACHED_TOME_KNOWLEDGE.add(s);
                }
            } catch (Exception e) {
                PECore.LOGGER.warn("Failed to cache knowledge for {}", stack);
                e.printStackTrace();
            }
        }
    }

    public static List<ItemStack> getCachedTomeKnowledge() {
        return Collections.unmodifiableList(CACHED_TOME_KNOWLEDGE);
    }
}

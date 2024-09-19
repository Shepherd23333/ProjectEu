package moze_intel.projecte.emc;

import moze_intel.projecte.PECore;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class FuelMapper {
    private static final List<SimpleStack> FUEL_MAP = new ArrayList<>();

    public static void loadMap() {
        FUEL_MAP.clear();

        for (ItemStack stack : OreDictionary.getOres("collectorFuels"))
            addToMap(stack);

        FUEL_MAP.sort(Comparator.comparing(EMCMapper::getEmcValue));
    }

    private static void addToMap(ItemStack stack) {
        if (EMCHelper.doesItemHaveEmc(stack)) {
            addToMap(new SimpleStack(stack));
        }
    }

    public static boolean isStackFuel(ItemStack stack) {
        return mapContains(new SimpleStack(stack));
    }

    public static boolean isStackMaxFuel(ItemStack stack) {
        return FUEL_MAP.indexOf(new SimpleStack(stack)) == FUEL_MAP.size() - 1;
    }

    public static ItemStack getFuelUpgrade(ItemStack stack) {
        SimpleStack fuel = new SimpleStack(stack);

        int index = FUEL_MAP.indexOf(fuel);

        if (index == -1) {
            PECore.LOGGER.warn("Tried to upgrade invalid fuel: {}", stack);
            return ItemStack.EMPTY;
        }

        int nextIndex = index == FUEL_MAP.size() - 1 ? 0 : index + 1;

        return FUEL_MAP.get(nextIndex).toItemStack();
    }

    private static void addToMap(SimpleStack stack) {
        if (stack.isValid()) {
            if (!FUEL_MAP.contains(stack)) {
                FUEL_MAP.add(stack);
            }
        }
    }

    private static boolean mapContains(SimpleStack stack) {
        return stack.isValid() && FUEL_MAP.contains(stack);
    }

    /**
     * @return An immutable version of the Fuel Map
     */
    public static List<SimpleStack> getFuelMap() {
        return Collections.unmodifiableList(FUEL_MAP);
    }
}

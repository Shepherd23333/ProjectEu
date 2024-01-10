package me.shepherd23333.projecte.integration.jei.mappers;

import me.shepherd23333.projecte.emc.FuelMapper;
import me.shepherd23333.projecte.emc.SimpleStack;
import me.shepherd23333.projecte.integration.jei.collectors.CollectorRecipeCategory;
import me.shepherd23333.projecte.integration.jei.collectors.FuelUpgradeRecipe;
import me.shepherd23333.projecte.utils.EMCHelper;
import net.minecraft.item.ItemStack;

public class JEIFuelMapper extends JEICompatMapper<FuelUpgradeRecipe> {
    public JEIFuelMapper() {
        super(CollectorRecipeCategory.UID);
    }

    public void refresh() {
        clear();
        for (SimpleStack stack : FuelMapper.getFuelMap()) {
            ItemStack fuelUpgrade = FuelMapper.getFuelUpgrade(stack.toItemStack());
            if (EMCHelper.getEmcValue(stack.toItemStack()) <= EMCHelper.getEmcValue(fuelUpgrade)) {
                addRecipe(new FuelUpgradeRecipe(stack.toItemStack(), fuelUpgrade));
            }
        }
    }
}
package me.shepherd23333.projecte.emc.mappers;

import me.shepherd23333.projecte.emc.IngredientMap;
import me.shepherd23333.projecte.emc.collector.IMappingCollector;
import me.shepherd23333.projecte.emc.json.NSSItem;
import me.shepherd23333.projecte.emc.json.NormalizedSimpleStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.common.config.Configuration;

import java.math.BigInteger;
import java.util.Map;

public class SmeltingMapper implements IEMCMapper<NormalizedSimpleStack, BigInteger> {
    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, BigInteger> mapper, Configuration config) {
        Map<ItemStack, ItemStack> smelting = FurnaceRecipes.instance().getSmeltingList();
        for (Map.Entry<ItemStack, ItemStack> entry : smelting.entrySet()) {
            ItemStack input = entry.getKey();
            ItemStack output = entry.getValue();
            if (input.isEmpty() || output.isEmpty()) {
                continue;
            }
            IngredientMap<NormalizedSimpleStack> map = new IngredientMap<>();
            NormalizedSimpleStack normInput = NSSItem.create(input);
            NormalizedSimpleStack normOutput = NSSItem.create(output);
            map.addIngredient(normInput, input.getCount());
            mapper.addConversion(output.getCount(), normOutput, map.getMap());
            if (config.getBoolean("doBackwardsMapping", "", false, "If X has a value and is smelted from Y, Y will get a value too. This is an experimental thing and might result in Mappings you did not expect/want to happen.")) {
                map = new IngredientMap<>();
                map.addIngredient(normOutput, output.getCount());
                mapper.addConversion(input.getCount(), normInput, map.getMap());
            }

        }
    }

    @Override
    public String getName() {
        return "SmeltingMapper";
    }

    @Override
    public String getDescription() {
        return "Add Conversions for `FurnaceRecipes`";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}

package me.shepherd23333.projecte.emc.mappers;

import me.shepherd23333.projecte.emc.collector.IMappingCollector;
import me.shepherd23333.projecte.emc.json.NormalizedSimpleStack;
import me.shepherd23333.projecte.impl.ConversionProxyImpl;
import net.minecraftforge.common.config.Configuration;

import java.util.List;
import java.util.Map;

public class APICustomConversionMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    @Override
    public String getName() {
        return "APICustomConversionMapper";
    }

    @Override
    public String getDescription() {
        return "Allows other Mods to add Recipes to the EMC Calculation.";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, Configuration config) {
        for (Map.Entry<String, List<ConversionProxyImpl.APIConversion>> entry : ConversionProxyImpl.instance.storedConversions.entrySet()) {
            if (config.getBoolean(entry.getKey(), "allow", true,
                    String.format("Allow Mod %s to add its %d Recipes to the EMC Calculation", entry.getKey(), entry.getValue().size()))) {
                for (ConversionProxyImpl.APIConversion apiConversion : entry.getValue()) {
                    mapper.addConversion(apiConversion.amount, apiConversion.output, apiConversion.ingredients);
                }
            }
        }

    }
}
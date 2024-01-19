package me.shepherd23333.projecte.emc.mappers;

import me.shepherd23333.projecte.PECore;
import me.shepherd23333.projecte.config.CustomEMCParser;
import me.shepherd23333.projecte.emc.collector.IMappingCollector;
import me.shepherd23333.projecte.emc.json.NormalizedSimpleStack;
import net.minecraftforge.common.config.Configuration;

import java.math.BigInteger;

public class CustomEMCMapper implements IEMCMapper<NormalizedSimpleStack, BigInteger> {
    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, BigInteger> mapper, Configuration config) {
        for (CustomEMCParser.CustomEMCEntry entry : CustomEMCParser.currentEntries.entries) {
            PECore.debugLog("Adding custom EMC value for {}: {}", entry.nss, entry.emc);
            mapper.setValueBefore(entry.nss, entry.emc);
        }
    }

    @Override
    public String getName() {
        return "CustomEMCMapper";
    }

    @Override
    public String getDescription() {
        return "Uses the `custom_emc.json` File to add EMC values.";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}

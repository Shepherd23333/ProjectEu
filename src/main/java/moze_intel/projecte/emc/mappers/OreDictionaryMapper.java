package moze_intel.projecte.emc.mappers;

import com.google.common.collect.Sets;
import moze_intel.projecte.emc.collector.IMappingCollector;
import moze_intel.projecte.emc.json.NSSItem;
import moze_intel.projecte.emc.json.NormalizedSimpleStack;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

import java.math.BigInteger;
import java.util.Set;

public class OreDictionaryMapper implements IEMCMapper<NormalizedSimpleStack, BigInteger> {

    private static final Set<String> BLACKLIST_EXCEPTIONS = Sets.newHashSet(

    );

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, BigInteger> mapper, Configuration config) {
        if (config.getBoolean("blacklistOres", "", true, "Set EMC=0 for everything that has an OD Name that starts with `ore` or `crushed`")) {
            //Black-list all ores/dusts
            for (String s : OreDictionary.getOreNames()) {
                if (s == null) {
                    continue;
                }

                if (s.startsWith("ore") || s.startsWith("crushed")) {
                    //Some exceptions in the black-listing
                    if (BLACKLIST_EXCEPTIONS.contains(s)) {
                        continue;
                    }

                    for (ItemStack stack : ItemHelper.getODItems(s)) {
                        if (stack.isEmpty()) {
                            continue;
                        }

                        mapper.setValueBefore(NSSItem.create(stack), BigInteger.ZERO);
                        mapper.setValueAfter(NSSItem.create(stack), BigInteger.ZERO);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "OreDictionaryMapper";
    }

    @Override
    public String getDescription() {
        return "Blacklist some OreDictionary names from getting an EMC value";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}

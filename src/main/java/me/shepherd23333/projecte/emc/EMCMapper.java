package me.shepherd23333.projecte.emc;

import me.shepherd23333.projecte.PECore;
import me.shepherd23333.projecte.api.event.EMCRemapEvent;
import me.shepherd23333.projecte.config.ProjectEConfig;
import me.shepherd23333.projecte.emc.arithmetics.FullBigFracArithmetic;
import me.shepherd23333.projecte.emc.arithmetics.IValueArithmetic;
import me.shepherd23333.projecte.emc.collector.BigIntToBigFracCollector;
import me.shepherd23333.projecte.emc.collector.DumpToFileCollector;
import me.shepherd23333.projecte.emc.collector.IExtendedMappingCollector;
import me.shepherd23333.projecte.emc.collector.WildcardSetValueFixCollector;
import me.shepherd23333.projecte.emc.generators.BigFracToBigIntGenerator;
import me.shepherd23333.projecte.emc.generators.IValueGenerator;
import me.shepherd23333.projecte.emc.json.NSSItem;
import me.shepherd23333.projecte.emc.json.NormalizedSimpleStack;
import me.shepherd23333.projecte.emc.mappers.*;
import me.shepherd23333.projecte.emc.mappers.customConversions.CustomConversionMapper;
import me.shepherd23333.projecte.emc.pregenerated.PregeneratedEMC;
import me.shepherd23333.projecte.playerData.Transmutation;
import me.shepherd23333.projecte.utils.PrefixConfiguration;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.math3.fraction.BigFraction;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public final class EMCMapper {
    public static final Map<SimpleStack, BigInteger> emc = new LinkedHashMap<>();

    public static double covalenceLoss = ProjectEConfig.difficulty.covalenceLoss;
    public static boolean covalenceLossRounding = ProjectEConfig.difficulty.covalenceLossRounding;

    public static void map() {
        List<IEMCMapper<NormalizedSimpleStack, BigInteger>> emcMappers = Arrays.asList(
                new OreDictionaryMapper(),
                APICustomEMCMapper.instance,
                new CustomConversionMapper(),
                new CustomEMCMapper(),
                new CraftingMapper(),
                new FluidMapper(),
                new SmeltingMapper(),
                new APICustomConversionMapper()
        );
        SimpleGraphMapper<NormalizedSimpleStack, BigFraction, IValueArithmetic<BigFraction>> mapper = new SimpleGraphMapper<>(new FullBigFracArithmetic());
        IValueGenerator<NormalizedSimpleStack, BigInteger> valueGenerator = new BigFracToBigIntGenerator<>(mapper);
        IExtendedMappingCollector<NormalizedSimpleStack, BigInteger, IValueArithmetic<BigFraction>> mappingCollector = new BigIntToBigFracCollector<>(mapper);
        mappingCollector = new WildcardSetValueFixCollector<>(mappingCollector);

        Configuration config = new Configuration(new File(PECore.CONFIG_DIR, "mapping.cfg"));
        config.load();

        if (config.getBoolean("dumpEverythingToFile", "general", false, "Want to take a look at the internals of EMC Calculation? Enable this to write all the conversions and setValue-Commands to config/ProjectE/mappingdump.json")) {
            mappingCollector = new DumpToFileCollector<>(new File(PECore.CONFIG_DIR, "mappingdump.json"), mappingCollector);
        }

        boolean shouldUsePregenerated = config.getBoolean("pregenerate", "general", false, "When the next EMC mapping occurs write the results to config/ProjectE/pregenerated_emc.json and only ever run the mapping again" +
                " when that file does not exist, this setting is set to false, or an error occurred parsing that file.");

        Map<NormalizedSimpleStack, BigInteger> graphMapperValues;
        if (shouldUsePregenerated && PECore.PREGENERATED_EMC_FILE.canRead() && PregeneratedEMC.tryRead(PECore.PREGENERATED_EMC_FILE, graphMapperValues = new HashMap<>())) {
            PECore.LOGGER.info(String.format("Loaded %d values from pregenerated EMC File", graphMapperValues.size()));
        } else {
            SimpleGraphMapper.setLogFoundExploits(config.getBoolean("logEMCExploits", "general", true,
                    "Log known EMC Exploits. This can not and will not find all possible exploits. " +
                            "This will only find exploits that result in fixed/custom emc values that the algorithm did not overwrite. " +
                            "Exploits that derive from conversions that are unknown to ProjectE will not be found."
            ));

            PECore.debugLog("Starting to collect Mappings...");
            for (IEMCMapper<NormalizedSimpleStack, BigInteger> emcMapper : emcMappers) {
                try {
                    if (config.getBoolean(emcMapper.getName(), "enabledMappers", emcMapper.isAvailable(), emcMapper.getDescription()) && emcMapper.isAvailable()) {
                        DumpToFileCollector.currentGroupName = emcMapper.getName();
                        emcMapper.addMappings(mappingCollector, new PrefixConfiguration(config, "mapperConfigurations." + emcMapper.getName()));
                        PECore.debugLog("Collected Mappings from " + emcMapper.getClass().getName());
                    }
                } catch (Exception e) {
                    PECore.LOGGER.fatal("Exception during Mapping Collection from Mapper {}. PLEASE REPORT THIS! EMC VALUES MIGHT BE INCONSISTENT!", emcMapper.getClass().getName());
                    e.printStackTrace();
                }
            }
            DumpToFileCollector.currentGroupName = "NSSHelper";
            NormalizedSimpleStack.addMappings(mappingCollector);

            PECore.debugLog("Mapping Collection finished");
            mappingCollector.finishCollection();

            PECore.debugLog("Starting to generate Values:");

            config.save();

            graphMapperValues = valueGenerator.generateValues();
            PECore.debugLog("Generated Values...");

            filterEMCMap(graphMapperValues);

            if (shouldUsePregenerated) {
                //Should have used pregenerated, but the file was not read => regenerate.
                try {
                    PregeneratedEMC.write(PECore.PREGENERATED_EMC_FILE, graphMapperValues);
                    PECore.debugLog("Wrote Pregen-file!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        for (Map.Entry<NormalizedSimpleStack, BigInteger> entry : graphMapperValues.entrySet()) {
            NSSItem normStackItem = (NSSItem) entry.getKey();
            Item obj = Item.REGISTRY.getObject(new ResourceLocation(normStackItem.itemName));
            if (obj != null) {
                emc.put(new SimpleStack(obj.getRegistryName(), normStackItem.damage), entry.getValue());
            } else {
                PECore.LOGGER.warn("Could not add EMC value for {}|{}. Can not get ItemID!", normStackItem.itemName, normStackItem.damage);
            }
        }

        MinecraftForge.EVENT_BUS.post(new EMCRemapEvent());
        Transmutation.cacheFullKnowledge();
        FuelMapper.loadMap();
        PECore.refreshJEI();
    }

    private static void filterEMCMap(Map<NormalizedSimpleStack, BigInteger> map) {
        map.entrySet().removeIf(e -> !(e.getKey() instanceof NSSItem)
                || ((NSSItem) e.getKey()).damage == OreDictionary.WILDCARD_VALUE
                || e.getValue().compareTo(BigInteger.ZERO) <= 0);
    }

    public static boolean mapContains(SimpleStack key) {
        return emc.containsKey(key);
    }

    public static BigInteger getEmcValue(SimpleStack stack) {
        return emc.get(stack);
    }

    public static void clearMaps() {
        emc.clear();
    }
}

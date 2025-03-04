package moze_intel.projecte.integration.mekanism;

import com.google.common.collect.ImmutableMap;
import mekanism.api.gas.Gas;
import mekanism.common.MekanismFluids;
import mekanism.common.MekanismItems;
import moze_intel.projecte.emc.collector.IMappingCollector;
import moze_intel.projecte.emc.json.NSSFake;
import moze_intel.projecte.emc.json.NSSItem;
import moze_intel.projecte.emc.json.NormalizedSimpleStack;
import moze_intel.projecte.emc.mappers.IEMCMapper;
import moze_intel.projecte.utils.Constants;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Optional;

import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class GasMapper implements IEMCMapper<NormalizedSimpleStack, BigInteger> {
    @Override
    public String getName() {
        return "GasMapper";
    }

    @Override
    public String getDescription() {
        return "Add conversions of gases.(Valid only if MekCEu is loaded.)";
    }

    @Override
    public boolean isAvailable() {
        return Constants.loadMek;
    }

    @Override
    @Optional.Method(modid = "mekanism")
    public void addMappings(IMappingCollector<NormalizedSimpleStack, BigInteger> mapper, Configuration config) {
        mapper.setValueBefore(NSSGas.create(MekanismFluids.Oxygen), Constants.FREE);
        mapper.setValueBefore(NSSGas.create(MekanismFluids.Chlorine), Constants.FREE);
        mapper.setValueBefore(NSSGas.create(MekanismFluids.UnstableDimensional), BigInteger.ONE);

        AtomicInteger i = new AtomicInteger();
        Func f = (Gas gas, int amount, BigInteger value) -> {
            NormalizedSimpleStack fakeGas = NSSFake.create("fakeGas_" + i);
            mapper.setValueBefore(fakeGas, value);
            mapper.addConversion(amount, NSSGas.create(gas), Collections.singletonList(fakeGas));
            i.getAndIncrement();
        };

        f.addEMC(MekanismFluids.SpentNuclearWaste, 1000, BigInteger.ONE);
        mapper.addConversion(700, NSSGas.create(MekanismFluids.Ethene), ImmutableMap.of(
                NSSItem.create(MekanismItems.BioFuel), 16
        ));
    }

    @FunctionalInterface
    interface Func {
        void addEMC(Gas gas, int amount, BigInteger value);
    }
}

package me.shepherd23333.projecte.emc.mappers;

import com.google.common.collect.ImmutableMap;
import me.shepherd23333.projecte.PECore;
import me.shepherd23333.projecte.emc.arithmetics.FullBigFracArithmetic;
import me.shepherd23333.projecte.emc.collector.IExtendedMappingCollector;
import me.shepherd23333.projecte.emc.collector.IMappingCollector;
import me.shepherd23333.projecte.emc.json.*;
import me.shepherd23333.projecte.utils.Constants;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FluidMapper implements IEMCMapper<NormalizedSimpleStack, BigInteger> {
    private static final List<Pair<NormalizedSimpleStack, FluidStack>> melting = new ArrayList<>();

    private static void addMelting(String odName, String fluidName, int amount) {
        addMelting(NSSOreDictionary.create(odName), fluidName, amount);
    }

    private static void addMelting(Item item, String fluidName, int amount) {
        addMelting(NSSItem.create(item), fluidName, amount);
    }

    private static void addMelting(Block block, String fluidName, int amount) {
        addMelting(NSSItem.create(block), fluidName, amount);
    }

    private static void addMelting(NormalizedSimpleStack stack, String fluidName, int amount) {
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid != null) {
            melting.add(Pair.of(stack, new FluidStack(fluid, amount)));
        } else {
            PECore.LOGGER.warn("Can not get Fluid '{}'", fluidName);
        }
    }

    static {
        addMelting(Blocks.OBSIDIAN, "obisidan.molten", 288);
        addMelting(Blocks.GLASS, "glass.molten", 1000);
        addMelting(Blocks.GLASS_PANE, "glass.molten", 250);
        addMelting(Items.ENDER_PEARL, "ender", 250);

        addMelting("ingotIron", "iron.molten", 144);
        addMelting("ingotGold", "gold.molten", 144);
        addMelting("ingotCopper", "copper.molten", 144);
        addMelting("ingotTin", "tin.molten", 144);
        addMelting("ingotSilver", "silver.molten", 144);
        addMelting("ingotLead", "lead.molten", 144);
        addMelting("ingotNickel", "nickel.molten", 144);
        addMelting("ingotAluminum", "aluminum.molten", 144);
        addMelting("ingotArdite", "ardite.molten", 144);
        addMelting("ingotCobalt", "cobalt.molten", 144);
        addMelting("ingotPlatinum", "platinum.molten", 144);
        addMelting("ingotObsidian", "obsidian.molten", 144);
        addMelting("ingotElectrum", "electrum.molten", 144);
        addMelting("ingotInvar", "invar.molten", 144);
        addMelting("ingotSignalum", "signalum.molten", 144);
        addMelting("ingotLumium", "lumium.molten", 144);
        addMelting("ingotEnderium", "enderium.molten", 144);
        addMelting("ingotMithril", "mithril.molten", 144);

        addMelting("ingotBronze", "bronze.molten", 144);
        addMelting("ingotAluminumBrass", "aluminumbrass.molten", 144);
        addMelting("ingotManyullyn", "manyullyn.molten", 144);
        addMelting("ingotAlumite", "alumite.molten", 144);

        addMelting("gemEmerald", "emerald.liquid", 640);
        addMelting("dustRedstone", "redstone", 100);
        addMelting("dustGlowstone", "glowstone", 250);

        addMelting("dustCryotheum", "cryotheum", 100);
        addMelting("dustPryotheum", "pryotheum", 100);
    }

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, BigInteger> mapper, Configuration config) {
        mapper.setValueBefore(NSSFluid.create(FluidRegistry.WATER), Constants.FREE/*=Free. TODO: Use IntArithmetic*/);
        //1 Bucket of Lava = 1 Block of Obsidian
        mapper.addConversion(1000, NSSFluid.create(FluidRegistry.LAVA), Collections.singletonList(NSSItem.create(Blocks.OBSIDIAN)));

        //Add Conversion in case MFR is not present and milk is not an actual fluid
        NormalizedSimpleStack fakeMilkFluid = NSSFake.create("fakeMilkFluid");
        mapper.setValueBefore(fakeMilkFluid, BigInteger.valueOf(16));
        mapper.addConversion(1, NSSItem.create(Items.MILK_BUCKET), Arrays.asList(NSSItem.create(Items.BUCKET), fakeMilkFluid));

        Fluid milkFluid = FluidRegistry.getFluid("milk");
        if (milkFluid != null) {
            mapper.addConversion(1000, NSSFluid.create(milkFluid), Collections.singletonList(fakeMilkFluid));
        }

        if (!(mapper instanceof IExtendedMappingCollector))
            throw new RuntimeException("Cannot add Extended Fluid Mappings to mapper!");
        IExtendedMappingCollector emapper = (IExtendedMappingCollector) mapper;
        FullBigFracArithmetic fluidArithmetic = new FullBigFracArithmetic();

        for (Pair<NormalizedSimpleStack, FluidStack> pair : melting) {
            emapper.addConversion(pair.getValue().amount, NSSFluid.create(pair.getValue().getFluid()), Collections.singletonList(pair.getKey()), fluidArithmetic);
        }

        // TODO figure out a way to get all containers again since FluidContainerRegistry disappeared after fluid caps
        mapper.addConversion(1, NSSItem.create(Items.WATER_BUCKET), ImmutableMap.of(NSSItem.create(Items.BUCKET), 1, NSSFluid.create(FluidRegistry.WATER), 1000));
        mapper.addConversion(1, NSSItem.create(Items.LAVA_BUCKET), ImmutableMap.of(NSSItem.create(Items.BUCKET), 1, NSSFluid.create(FluidRegistry.LAVA), 1000));
        if (milkFluid != null) {
            mapper.addConversion(1, NSSItem.create(Items.MILK_BUCKET), ImmutableMap.of(NSSItem.create(Items.BUCKET), 1, NSSFluid.create(milkFluid), 1000));
        }
    }

    @Override
    public String getName() {
        return "FluidMapper";
    }

    @Override
    public String getDescription() {
        return "Adds Conversions for fluid container items and fluids.";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
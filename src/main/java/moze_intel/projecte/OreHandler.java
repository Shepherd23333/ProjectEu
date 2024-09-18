package moze_intel.projecte;

import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreHandler {
    public static void register() {
        OreDictionary.registerOre("collectorFuels", Items.REDSTONE);
        OreDictionary.registerOre("collectorFuels", Blocks.REDSTONE_BLOCK);
        OreDictionary.registerOre("collectorFuels", Items.COAL);
        OreDictionary.registerOre("collectorFuels", new ItemStack(Items.COAL, 1, 1));
        OreDictionary.registerOre("collectorFuels", Blocks.COAL_BLOCK);
        OreDictionary.registerOre("collectorFuels", Items.GUNPOWDER);
        OreDictionary.registerOre("collectorFuels", Items.BLAZE_POWDER);
        OreDictionary.registerOre("collectorFuels", Items.GLOWSTONE_DUST);
        OreDictionary.registerOre("collectorFuels", Blocks.GLOWSTONE);
        OreDictionary.registerOre("collectorFuels", ObjHandler.fuels);
        OreDictionary.registerOre("collectorFuels", ObjHandler.fuelBlock);
        OreDictionary.registerOre("collectorFuels", new ItemStack(ObjHandler.fuels, 1, 1));
        OreDictionary.registerOre("collectorFuels", new ItemStack(ObjHandler.fuelBlock, 1, 1));
        OreDictionary.registerOre("collectorFuels", new ItemStack(ObjHandler.fuels, 1, 2));
        OreDictionary.registerOre("collectorFuels", new ItemStack(ObjHandler.fuelBlock, 1, 2));
    }
}

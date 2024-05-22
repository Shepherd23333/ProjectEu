package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;

public class KleinStar extends ItemPE implements IItemEmc {
    public KleinStar() {
        this.setTranslationKey("klein_star");
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return stack.hasTagCompound();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        BigInteger starEmc = getEmc(stack);

        if (starEmc.equals(BigInteger.ZERO)) {
            return 1.0D;
        }

        return BigDecimal.ONE.subtract(
                new BigDecimal(starEmc).divide(new BigDecimal(EMCHelper.getKleinStarMaxEmc(stack)))
        ).doubleValue();
    }


    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote && PECore.DEV_ENVIRONMENT) {
            setEmc(stack, EMCHelper.getKleinStarMaxEmc(stack));
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
        }

        return ActionResult.newResult(EnumActionResult.PASS, stack);
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack stack) {
        if (stack.getItemDamage() > 5) {
            return "pe.debug.metainvalid";
        }

        return super.getTranslationKey() + "_" + (stack.getItemDamage() + 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs cTab, NonNullList<ItemStack> list) {
        if (isInCreativeTab(cTab)) {
            for (int i = 0; i < 6; ++i) {
                list.add(new ItemStack(this, 1, i));
            }
        }
    }

    public enum EnumKleinTier {
        EIN("ein"),
        ZWEI("zwei"),
        DREI("drei"),
        VIER("vier"),
        SPHERE("sphere"),
        OMEGA("omega");

        public final String name;

        EnumKleinTier(String name) {
            this.name = name;
        }
    }

    // -- IItemEmc -- //

    @Override
    public BigInteger addEmc(@Nonnull ItemStack stack, BigInteger toAdd) {
        BigInteger add = getMaximumEMC(stack).subtract(getStoredEMC(stack)).min(toAdd);
        addEmcToStack(stack, add);
        return add;
    }

    @Override
    public BigInteger extractEmc(@Nonnull ItemStack stack, BigInteger toRemove) {
        BigInteger sub = getStoredEMC(stack).min(toRemove);
        removeEmc(stack, sub);
        return sub;
    }

    @Override
    public BigInteger getStoredEMC(@Nonnull ItemStack stack) {
        return getEmc(stack);
    }

    @Override
    public BigInteger getMaximumEMC(@Nonnull ItemStack stack) {
        return EMCHelper.getKleinStarMaxEmc(stack);
    }
}

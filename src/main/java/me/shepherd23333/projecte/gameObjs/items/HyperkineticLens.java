package me.shepherd23333.projecte.gameObjs.items;

import me.shepherd23333.projecte.api.PESounds;
import me.shepherd23333.projecte.api.item.IItemCharge;
import me.shepherd23333.projecte.api.item.IProjectileShooter;
import me.shepherd23333.projecte.gameObjs.entity.EntityLensProjectile;
import me.shepherd23333.projecte.utils.Constants;
import me.shepherd23333.projecte.utils.PlayerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class HyperkineticLens extends ItemPE implements IProjectileShooter, IItemCharge {
    public HyperkineticLens() {
        this.setTranslationKey("hyperkinetic_lens");
        this.setMaxStackSize(1);
        this.setNoRepair();
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!world.isRemote && shootProjectile(player, stack, hand)) {
            PlayerHelper.swingItem(player, hand);
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean shootProjectile(@Nonnull EntityPlayer player, @Nonnull ItemStack stack, EnumHand hand) {
        World world = player.getEntityWorld();
        long requiredEmc = Constants.EXPLOSIVE_LENS_COST[this.getCharge(stack)];

        if (!consumeFuel(player, stack, requiredEmc, true)) {
            return false;
        }

        world.playSound(null, player.posX, player.posY, player.posZ, PESounds.POWER, SoundCategory.PLAYERS, 1.0F, 1.0F);
        EntityLensProjectile ent = new EntityLensProjectile(world, player, this.getCharge(stack));
        ent.shoot(player, player.rotationPitch, player.rotationYaw, 0, 1.5F, 1);
        world.spawnEntity(ent);
        return true;
    }

    @Override
    public int getNumCharges(@Nonnull ItemStack stack) {
        return 3;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - (double) getCharge(stack) / getNumCharges(stack);
    }
}

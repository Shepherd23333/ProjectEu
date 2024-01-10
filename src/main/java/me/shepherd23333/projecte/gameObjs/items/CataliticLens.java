package me.shepherd23333.projecte.gameObjs.items;

import me.shepherd23333.projecte.api.item.IProjectileShooter;
import me.shepherd23333.projecte.gameObjs.ObjHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import javax.annotation.Nonnull;

public class CataliticLens extends DestructionCatalyst implements IProjectileShooter {
    public CataliticLens() {
        this.setTranslationKey("catalitic_lens");
    }

    @Override
    public boolean shootProjectile(@Nonnull EntityPlayer player, @Nonnull ItemStack stack, EnumHand hand) {
        return ((IProjectileShooter) ObjHandler.hyperLens).shootProjectile(player, stack, hand);
    }

    @Override
    public int getNumCharges(@Nonnull ItemStack stack) {
        return 7;
    }
}

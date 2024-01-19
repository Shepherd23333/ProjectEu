package me.shepherd23333.projecte.gameObjs.container;

import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.math.BigInteger;

public abstract class BigIntegerContainer extends Container {
    @SideOnly(Side.CLIENT)
    public void updateProgressBarBigInteger(int id, BigInteger data) {
    }
}

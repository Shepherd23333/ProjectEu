package moze_intel.projecte.gameObjs.container;

import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.math.BigInteger;

public abstract class BigIntContainer extends Container {
    @SideOnly(Side.CLIENT)
    public void updateProgressBarBigInt(int id, BigInteger data) {
    }
}

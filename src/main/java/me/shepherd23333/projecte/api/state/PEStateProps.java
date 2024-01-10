package me.shepherd23333.projecte.api.state;

import me.shepherd23333.projecte.api.state.enums.EnumFuelType;
import me.shepherd23333.projecte.api.state.enums.EnumMatterType;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.EnumFacing;

public final class PEStateProps {

    public static final IProperty<EnumFacing> FACING = BlockHorizontal.FACING;
    public static final IProperty<EnumFuelType> FUEL_PROP = PropertyEnum.create("fueltype", EnumFuelType.class);
    public static final IProperty<EnumMatterType> TIER_PROP = PropertyEnum.create("tier", EnumMatterType.class);

    private PEStateProps() {
    }

}

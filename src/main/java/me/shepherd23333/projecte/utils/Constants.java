package me.shepherd23333.projecte.utils;

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;

public final class Constants {
    public static final BigInteger MAX_EXACT_TRANSMUTATION_DISPLAY = BigInteger.valueOf(1_000_000_000_000L);
    public static final BigInteger MAX_INTEGER = BigInteger.valueOf(Integer.MAX_VALUE);
    public static final BigInteger FREE = BigInteger.valueOf(Long.MIN_VALUE);
    public static final BigInteger MULTIPLE = BigInteger.valueOf(4);
    public static final BigInteger cons1 = BigInteger.valueOf(64);
    public static final BigFraction eps = new BigFraction(0, 200);
    public static final BigInteger MAX_KLEIN_EMC = BigInteger.valueOf(50000);
    public static final BigInteger RELAY_KLEIN_CHARGE_RATE = BigInteger.valueOf(16);
    public static final float[] COLLECTOR_LIGHT_VALS = new float[]{0.4375F, 0.6875F, 1.0F};
    public static final BigInteger TILE_MAX_EMC = BigInteger.valueOf(Long.MAX_VALUE);
    public static final int loopTimes = 1 << 6;

    public static final float[] EXPLOSIVE_LENS_RADIUS = new float[]{4.0F, 8.0F, 12.0F, 16.0F, 16.0F, 16.0F, 16.0F, 16.0F};
    public static final long[] EXPLOSIVE_LENS_COST = new long[]{384, 768, 1536, 2304, 2304, 2304, 2304, 2304};

    public static final BigInteger COLLECTOR_MK1_MAX = BigInteger.valueOf(10000);
    public static final BigInteger COLLECTOR_MK2_MAX = COLLECTOR_MK1_MAX.multiply(MULTIPLE);
    public static final BigInteger COLLECTOR_MK3_MAX = COLLECTOR_MK2_MAX.multiply(MULTIPLE);
    public static final BigInteger COLLECTOR_MK1_GEN = BigInteger.valueOf(4);
    public static final BigInteger COLLECTOR_MK2_GEN = COLLECTOR_MK1_GEN.multiply(MULTIPLE);
    public static final BigInteger COLLECTOR_MK3_GEN = COLLECTOR_MK2_GEN.multiply(MULTIPLE);

    public static final BigInteger RELAY_MK1_OUTPUT = BigInteger.valueOf(64);
    public static final BigInteger RELAY_MK2_OUTPUT = RELAY_MK1_OUTPUT.multiply(MULTIPLE);
    public static final BigInteger RELAY_MK3_OUTPUT = RELAY_MK2_OUTPUT.multiply(MULTIPLE);
    public static final BigInteger RELAY_MK1_MAX = BigInteger.valueOf(100000);
    public static final BigInteger RELAY_MK2_MAX = RELAY_MK1_MAX.multiply(MULTIPLE);
    public static final BigInteger RELAY_MK3_MAX = RELAY_MK2_MAX.multiply(MULTIPLE);

    public static final int COAL_BURN_TIME = 1600;
    public static final int ALCH_BURN_TIME = COAL_BURN_TIME * 4;
    public static final int MOBIUS_BURN_TIME = ALCH_BURN_TIME * 4;
    public static final int AETERNALIS_BURN_TIME = MOBIUS_BURN_TIME * 4;

    public static final int ALCH_CHEST_GUI = 0;
    public static final int ALCH_BAG_GUI = 1;
    public static final int TRANSMUTE_STONE_GUI = 2;
    public static final int CONDENSER_GUI = 3;
    public static final int RM_FURNACE_GUI = 4;
    public static final int DM_FURNACE_GUI = 5;
    public static final int COLLECTOR1_GUI = 6;
    public static final int COLLECTOR2_GUI = 7;
    public static final int COLLECTOR3_GUI = 8;
    public static final int RELAY1_GUI = 9;
    public static final int RELAY2_GUI = 10;
    public static final int RELAY3_GUI = 11;
    public static final int MERCURIAL_GUI = 12;
    public static final int PHILOS_STONE_GUI = 13;
    public static final int TRANSMUTATION_GUI = 14;
    public static final int ETERNAL_DENSITY_GUI = 15;
    public static final int CONDENSER_MK2_GUI = 16;
    public static final int MAX_CONDENSER_PROGRESS = 102;

    public static final int MAX_VEIN_SIZE = 250;

    public static final long ENCH_EMC_BONUS = 5000;
}

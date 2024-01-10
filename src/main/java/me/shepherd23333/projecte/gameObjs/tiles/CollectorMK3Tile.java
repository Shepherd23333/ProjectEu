package me.shepherd23333.projecte.gameObjs.tiles;

import me.shepherd23333.projecte.utils.Constants;

public class CollectorMK3Tile extends CollectorMK1Tile {
    public CollectorMK3Tile() {
        super(Constants.COLLECTOR_MK3_MAX, Constants.COLLECTOR_MK3_GEN);
    }

    @Override
    protected int getInvSize() {
        return 16;
    }
}

package me.shepherd23333.projecte.gameObjs.tiles;

import me.shepherd23333.projecte.utils.Constants;

public class CollectorMK2Tile extends CollectorMK1Tile {
    public CollectorMK2Tile() {
        super(Constants.COLLECTOR_MK2_MAX, Constants.COLLECTOR_MK2_GEN);
    }

    @Override
    protected int getInvSize() {
        return 12;
    }
}

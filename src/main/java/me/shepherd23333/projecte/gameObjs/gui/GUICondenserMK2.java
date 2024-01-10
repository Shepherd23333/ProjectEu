package me.shepherd23333.projecte.gameObjs.gui;

import me.shepherd23333.projecte.PECore;
import me.shepherd23333.projecte.gameObjs.container.CondenserMK2Container;
import me.shepherd23333.projecte.gameObjs.tiles.CondenserMK2Tile;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUICondenserMK2 extends GUICondenser {
    public GUICondenserMK2(InventoryPlayer invPlayer, CondenserMK2Tile tile) {
        super(new CondenserMK2Container(invPlayer, tile), new ResourceLocation(PECore.MODID.toLowerCase(), "textures/gui/condenser_mk2.png"));
    }
}

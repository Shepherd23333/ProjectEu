package me.shepherd23333.projecte.events;

import me.shepherd23333.projecte.PECore;
import me.shepherd23333.projecte.network.PacketHandler;
import me.shepherd23333.projecte.network.packets.KeyPressPKT;
import me.shepherd23333.projecte.utils.ClientKeyHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = PECore.MODID)
public class KeyPressEvent {
    @SubscribeEvent
    public static void keyPress(KeyInputEvent event) {
        for (KeyBinding k : ClientKeyHelper.mcToPe.keySet()) {
            if (k.isPressed()) {
                PacketHandler.sendToServer(new KeyPressPKT(ClientKeyHelper.mcToPe.get(k)));
            }
        }
    }
}

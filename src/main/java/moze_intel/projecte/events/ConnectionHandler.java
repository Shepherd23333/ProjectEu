package moze_intel.projecte.events;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import moze_intel.projecte.handlers.PlayerChecks;
import moze_intel.projecte.handlers.PlayerTimers;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.ClientCheckUpdatePKT;
import moze_intel.projecte.utils.PELogger;
import net.minecraft.entity.player.EntityPlayerMP;

public class ConnectionHandler
{
	@SubscribeEvent
	public void playerConnect(PlayerLoggedInEvent event)
	{
		PacketHandler.sendFragmentedEmcPacket((EntityPlayerMP) event.player);
		PacketHandler.sendTo(new ClientCheckUpdatePKT(), (EntityPlayerMP) event.player);

		PlayerTimers.registerPlayer(event.player);

	}

	@SubscribeEvent
	public void playerDisconnect(PlayerEvent.PlayerLoggedOutEvent event)
	{
		PlayerTimers.removePlayer(event.player);

		PELogger.logInfo("Removing " + event.player.getName() + " from scheduled checklists: Player disconnected.");
		PlayerChecks.removePlayerFromLists(event.player.getName());
	}

}

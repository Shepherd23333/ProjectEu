package moze_intel.projecte.network.commands;

import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.ClientKnowledgeClearPKT;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.utils.ChatHelper;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class ClearKnowledgeCMD extends ProjectEBaseCMD
{
	@Override
	public String getCommandName() 
	{
		return "projecte_clearKnowledge";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "pe.command.clearknowledge.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] params) 
	{
		if (params.length == 0)
		{
			if (sender instanceof EntityPlayerMP)
			{
				Transmutation.clearKnowledge(sender.getName());
				PacketHandler.sendTo(new ClientKnowledgeClearPKT(sender.getName()), (EntityPlayerMP) sender);
				sendSuccess(sender, new ChatComponentTranslation("pe.command.clearknowledge.success", sender.getName()));
			}
			else
			{
				sendError(sender, new ChatComponentTranslation("pe.command.clearknowledge.error", sender.getName()));
			}
		}
		else
		{
			for (Object obj : sender.getEntityWorld().playerEntities)
			{
				EntityPlayer player = (EntityPlayer) obj;
				
				if (player.getName().equalsIgnoreCase(params[0]))
				{
					Transmutation.clearKnowledge(player.getName());
					PacketHandler.sendTo(new ClientKnowledgeClearPKT(player.getName()), (EntityPlayerMP) player);
					sendSuccess(sender, new ChatComponentTranslation("pe.command.clearknowledge.success", player.getName()));
					
					if (!player.getName().equals(sender.getName()))
					{
						player.addChatComponentMessage(ChatHelper.modifyColor(new ChatComponentTranslation("pe.command.clearknowledge.notify", sender.getName()), EnumChatFormatting.RED));
					}
					
					return;
				}
			}

			sendError(sender, new ChatComponentTranslation("pe.command.clearknowledge.playernotfound", params[0]));
		}
	}

	@Override
	public int getRequiredPermissionLevel() 
	{
		return 4;
	}
}

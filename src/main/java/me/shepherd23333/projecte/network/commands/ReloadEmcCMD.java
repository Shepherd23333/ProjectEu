package me.shepherd23333.projecte.network.commands;

import me.shepherd23333.projecte.config.CustomEMCParser;
import me.shepherd23333.projecte.emc.EMCMapper;
import me.shepherd23333.projecte.network.PacketHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public class ReloadEmcCMD extends CommandBase {
    @Nonnull
    @Override
    public String getName() {
        return "reloadEMC";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/projecte reloadEMC";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] params) {
        sender.sendMessage(new TextComponentTranslation("pe.command.reload.started"));

        EMCMapper.clearMaps();
        CustomEMCParser.init();
        EMCMapper.map();

        sender.sendMessage(new TextComponentTranslation("pe.command.reload.success"));

        PacketHandler.sendFragmentedEmcPacketToAll();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
}

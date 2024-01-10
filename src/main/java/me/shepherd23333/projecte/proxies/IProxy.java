package me.shepherd23333.projecte.proxies;

import me.shepherd23333.projecte.api.capabilities.IAlchBagProvider;
import me.shepherd23333.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayer;

public interface IProxy {
    void registerKeyBinds();

    void registerRenderers();

    void registerLayerRenderers();

    void initializeManual();

    void clearClientKnowledge();

    IKnowledgeProvider getClientTransmutationProps();

    IAlchBagProvider getClientBagProps();

    EntityPlayer getClientPlayer();

    boolean isJumpPressed();
}

package me.shepherd23333.projecte.network.packets;

import io.netty.buffer.ByteBuf;
import me.shepherd23333.projecte.gameObjs.container.BigIntegerContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.math.BigInteger;

public class UpdateWindowBigIntegerPKT implements IMessage {
    private short windowId;
    private short propId;
    private BigInteger propVal;

    public UpdateWindowBigIntegerPKT() {
    }

    public UpdateWindowBigIntegerPKT(short windowId, short propId, BigInteger propVal) {
        this.windowId = windowId;
        this.propId = propId;
        this.propVal = propVal;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readUnsignedByte();
        propId = buf.readShort();
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        propVal = length > 0 ? new BigInteger(bytes) : BigInteger.ZERO;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeShort(propId);
        byte[] bytes = propVal.toByteArray();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static class Handler implements IMessageHandler<UpdateWindowBigIntegerPKT, IMessage> {
        @Override
        public IMessage onMessage(final UpdateWindowBigIntegerPKT msg, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    if (player.openContainer != null && player.openContainer.windowId == msg.windowId) {
                        //It should always be a LongContainer if it is this type of packet, if not fallback to normal update
                        if (player.openContainer instanceof BigIntegerContainer) {
                            ((BigIntegerContainer) player.openContainer).updateProgressBarBigInteger(msg.propId, msg.propVal);
                        } else {
                            player.openContainer.updateProgressBar(msg.propId, msg.propVal.intValueExact());
                        }
                    }
                }
            });
            return null;
        }
    }
}

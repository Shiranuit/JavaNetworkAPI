package Network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Client {
    public Net net;
    public ByteBuf buf;
    private Thread messages;
    public String uuid;
    private HashMap<Object, Method> messageHandler = new HashMap<Object, Method>();
    public Client() {
        buf = Unpooled.buffer();
    }

    public void registerMessageHandler(Object o) {
        if (o != null) {
            Class mhandler = o.getClass();
            for (Method m : mhandler.getMethods()) {
                for (Annotation a : m.getDeclaredAnnotations()) {
                    if (a.annotationType().equals(NetworkEvent.class)) {
                        if (m.getAnnotation(NetworkEvent.class).value() == NetworkSide.CLIENT) {
                            if (m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(MessageEvent.class)) {
                                messageHandler.put(o, m);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isClosed() {
        return net.isClosed();
    }

    public void close() throws Exception {
        net.close();
        if (messages != null)
            messages.interrupt();
    }

    public void connect(String ip, int port) throws Exception{
        net = new Net(ip, port);
        MessageEvent uuidbyte = this.receive();
        this.uuid = this.readString();
        messages = new Thread(new MessageReceiver(this));
        messages.start();
    }

    public MessageEvent receive() throws Exception{
        net.receive(5);
        int size = net.receiveBuff.readInt();
        byte type = net.receiveBuff.readByte();
        NetworkSide side;
        if (type == 0) {
            side = NetworkSide.SERVER;
        } else if (type == 1) {
            side = NetworkSide.CLIENT;
        } else {
            side = NetworkSide.NULL;
        }
        byte[] data = net.receive(size);
        for (Object o : this.messageHandler.keySet()) {
            Method m = this.messageHandler.get(o);
            m.invoke(o, new MessageEvent(new DataBuffer(data), side));
        }
        return new MessageEvent(new DataBuffer(data), side);
    }

    public void onMessage(MessageEvent event) {

    }

    public void writeInt(int v) {
        buf.writeInt(v);
    }

    public void writeBoolean(boolean v) {
        buf.writeBoolean(v);
    }

    public void writeLong(long v) {
        buf.writeLong(v);
    }

    public void writeDouble(double v) {
        buf.writeDouble(v);
    }

    public void writeShort(short v) {
        buf.writeShort(v);
    }

    public void writeByte(byte v) {
        buf.writeByte(v);
    }

    public void writeChar(char v) {
        buf.writeChar(v);
    }

    public void writeString(String v) {
        BufUtils.writeString(buf, v);
    }
    
    public void writeUTF8(String v) {
        BufUtils.writeUTF8String(buf, v);
    }

    public int readInt() {
        return net.receiveBuff.readInt();
    }

    public byte readByte() {
        return net.receiveBuff.readByte();
    }

    public boolean readBoolean() {
        return net.receiveBuff.readBoolean();
    }

    public long readLong() {
        return net.receiveBuff.readLong();
    }

    public short readShort() {
        return net.receiveBuff.readShort();
    }

    public double readDouble() {
        return net.receiveBuff.readDouble();
    }

    public char readChar() {
        return net.receiveBuff.readChar();
    }

    public String readString() {
        return BufUtils.readString(net.receiveBuff);
    }
    
    public String readUTF8() {
        return BufUtils.readUTF8String(net.receiveBuff);
    }

    public void sendToServer(byte[] b)throws Exception {
        net.sendBuff.writeInt(b.length);
        net.sendBuff.writeByte(0);
        net.sendBuff.writeInt(0);
        net.send();
        net.send(b);
    }

    public void sendToServer(DataBuffer b)throws Exception {
        net.sendBuff.writeInt(b.data.length);
        net.sendBuff.writeByte(0);
        net.sendBuff.writeInt(0);
        net.send();
        net.send(b);
    }

    public void sendToServer(ByteBuf b)throws Exception {
        net.sendBuff.writeInt(ByteBufUtil.getBytes(b).length);
        net.sendBuff.writeByte(0);
        net.sendBuff.writeInt(0);
        net.send();
        net.send(b);
    }

    public void sendToServer() throws Exception {
        net.sendBuff.writeInt(ByteBufUtil.getBytes(buf).length);
        net.sendBuff.writeByte(0);
        net.sendBuff.writeInt(0);
        net.send();
        net.send(buf);
        this.buf = Unpooled.buffer();
    }

    public void sendToAllClient(byte[] b)throws Exception {
        net.sendBuff.writeInt(b.length);
        net.sendBuff.writeByte(1);
        net.sendBuff.writeInt(0);
        net.send();
        net.send(b);
    }

    public void sendToAllClient(DataBuffer b)throws Exception {
        net.sendBuff.writeInt(b.data.length);
        net.sendBuff.writeByte(1);
        net.sendBuff.writeInt(0);
        net.send();
        net.send(b);
    }

    public void sendToAllClient(ByteBuf b)throws Exception {
        net.sendBuff.writeInt(ByteBufUtil.getBytes(b).length);
        net.sendBuff.writeByte(1);
        net.sendBuff.writeInt(0);
        net.send();
        net.send(b);
    }

    public void sendToAllClient() throws Exception {
        net.sendBuff.writeInt(ByteBufUtil.getBytes(buf).length);
        net.sendBuff.writeByte(1);
        net.sendBuff.writeInt(0);
        net.send();
        net.send(buf);
        this.buf = Unpooled.buffer();
    }

    public void sendToClient(byte[] b, String uuid)throws Exception {
        net.sendBuff.writeInt(b.length);
        net.sendBuff.writeByte(2);
        net.sendBuff.writeInt(uuid.length()*2+4);
        BufUtils.writeString(net.sendBuff, uuid);
        net.send();
        net.send(b);
    }

    public void sendToClient(DataBuffer buf, String uuid)throws Exception {
        net.sendBuff.writeInt(buf.data.length);
        net.sendBuff.writeByte(2);
        net.sendBuff.writeInt(uuid.length()*2+4);
        BufUtils.writeString(net.sendBuff, uuid);
        net.send();
        net.send(buf.data);
    }

    public void sendToClient(ByteBuf b, String uuid)throws Exception {
        net.sendBuff.writeInt(ByteBufUtil.getBytes(b).length);
        net.sendBuff.writeByte(2);
        net.sendBuff.writeInt(uuid.length()*2+4);
        BufUtils.writeString(net.sendBuff, uuid);
        net.send();
        net.send(b);
    }

    public void sendToClient(String uuid) throws Exception {
        net.sendBuff.writeInt(ByteBufUtil.getBytes(buf).length);
        net.sendBuff.writeByte(2);
        net.sendBuff.writeInt(uuid.length()*2+4);
        BufUtils.writeString(net.sendBuff, uuid);
        net.send();
        net.send(buf);
        this.buf = Unpooled.buffer();
    }
}

class MessageReceiver implements Runnable {
    Client me;

    public MessageReceiver(Client me) {
        this.me = me;
    }

    @Override
    public void run() {
        while (!me.isClosed()) {
            try {
                MessageEvent data = me.receive();
                me.onMessage(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
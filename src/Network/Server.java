package Network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.*;
import java.util.HashMap;
import java.util.UUID;

public class Server {
    ByteBuf sendBuf;
    Server me;
    ServerSocket server;
    boolean isRunning = false;
    HashMap<String, Net> nets = new HashMap<String, Net>();
    HashMap<Object, Method> messageHandler = new HashMap<Object, Method>();

    public int clientCount() {
        return nets.size();
    }

    public Server(int port) throws Exception {
    	server = new ServerSocket(port);
    	me = this;
    	this.sendBuf = Unpooled.buffer();
    }
    public Server(int port, int file) throws Exception {
        server = new ServerSocket(port, file);
        me = this;
        this.sendBuf = Unpooled.buffer();
    }
    public Server(int port, int file, String bindIP) throws Exception {
        server = new ServerSocket(port, file, InetAddress.getByName(bindIP));
        me = this;
        this.sendBuf = Unpooled.buffer();
    }

    public int getPort() {
        return server.getLocalPort();
    }

    public boolean isClosed() {
        return server.isClosed();
    }

    public void stop() throws Exception {
        isRunning = false;
        server.close();
    }

    public void writeInt(int v) {
        sendBuf.writeInt(v);
    }

    public void writeBoolean(boolean v) {
        sendBuf.writeBoolean(v);
    }

    public void writeLong(long v) {
        sendBuf.writeLong(v);
    }

    public void writeDouble(double v) {
        sendBuf.writeDouble(v);
    }

    public void writeShort(short v) {
        sendBuf.writeShort(v);
    }

    public void writeByte(byte v) {
        sendBuf.writeByte(v);
    }

    public void writeChar(char v) {
        sendBuf.writeChar(v);
    }

    public void writeString(String v) {
        BufUtils.writeString(sendBuf, v);
    }



    public void sendToClient(String uuid, byte[] data) throws Exception {
        Net destinationClient = this.nets.get(uuid);
        destinationClient.sendBuff.writeInt(data.length);
        destinationClient.sendBuff.writeByte(0);
        destinationClient.send();
        destinationClient.send(data);
    }

    public void sendToClient(String uuid, ByteBuf bytes) throws Exception {
        DataBuffer buf = new DataBuffer(bytes);
        Net destinationClient = this.nets.get(uuid);
        destinationClient.sendBuff.writeInt(buf.data.length);
        destinationClient.sendBuff.writeByte(0);
        destinationClient.send();
        destinationClient.send(buf);
    }

    public void sendToClient(String uuid, DataBuffer buf) throws Exception {
        Net destinationClient = this.nets.get(uuid);
        destinationClient.sendBuff.writeInt(buf.data.length);
        destinationClient.sendBuff.writeByte(0);
        destinationClient.send();
        destinationClient.send(buf);
    }

    public void sendToClient(String uuid) throws Exception {
        Net destinationClient = this.nets.get(uuid);
        destinationClient.sendBuff.writeInt(ByteBufUtil.getBytes(sendBuf).length);
        destinationClient.sendBuff.writeByte(0);
        destinationClient.send();
        destinationClient.send(sendBuf);
        sendBuf = Unpooled.buffer();
    }

    public void sendToAllClients(byte[] data) throws Exception {
        for (String uuid : this.nets.keySet()) {
            Net net = this.nets.get(uuid);
            net.sendBuff.writeInt(data.length);
            net.sendBuff.writeByte(0);
            net.send();
            net.send(data);
        }
    }

    public void sendToAllClients(ByteBuf bytes) throws Exception {
        for (String uuid : this.nets.keySet()) {
            Net net = this.nets.get(uuid);
            net.sendBuff.writeInt(ByteBufUtil.getBytes(bytes).length);
            net.sendBuff.writeByte(0);
            net.send();
            net.send(bytes);
        }
    }

    public void sendToAllClients(DataBuffer buf) throws Exception {
        for (String uuid : this.nets.keySet()) {
            Net net = this.nets.get(uuid);
            net.sendBuff.writeInt(buf.data.length);
            net.sendBuff.writeByte(0);
            net.send();
            net.send(buf);
        }
    }

    public void sendToAllClients() throws Exception {
        for (String uuid : this.nets.keySet()) {
            Net net = this.nets.get(uuid);
                net.sendBuff.writeInt(ByteBufUtil.getBytes(sendBuf).length);
                net.sendBuff.writeByte(0);
                net.send();
                net.send(sendBuf);
        }
        sendBuf = Unpooled.buffer();
    }

    public void start() throws Exception {
        isRunning = true;
        Thread proccess = new Thread(new Runnable(){
            public void run(){
                while(isRunning == true){
                    try {
                        Net client = new Net(server.accept());
                        UUID clientUUID = UUID.randomUUID();
                        nets.put(clientUUID.toString(), client);
                        Thread clientProcess = new Thread(new ClientProcessor(client,me));
                        clientProcess.start();
                        ByteBuf buf = Unpooled.buffer();
                        BufUtils.writeString(buf,clientUUID.toString());
                        client.sendBuff.writeInt(ByteBufUtil.getBytes(buf).length);
                        client.sendBuff.writeByte(0);
                        client.send();
                        client.send(buf);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                
                try {
                    server.close();
                 } catch (IOException e) {
                    e.printStackTrace();
                    server = null;
                 }
            }
        });

        proccess.start();
    }

    public void onMessage(MessageEvent event) {

    }

    public void registerMessageHandler(Object o) {
        if (o != null) {
            Class mhandler = o.getClass();
            for (Method m : mhandler.getMethods()) {
                for (Annotation a : m.getDeclaredAnnotations()) {
                    if (a.annotationType().equals(NetworkEvent.class)) {
                        if (m.getAnnotation(NetworkEvent.class).value() == NetworkSide.SERVER) {
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


}


class ClientProcessor implements Runnable{

    private Net sock;
    private PrintWriter writer = null;
    private BufferedInputStream reader = null;
    private Server server;

    public ClientProcessor(Net pSock, Server server){
        sock = pSock;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            while(!sock.isClosed()){
                UUID clientUUID = null;
                for (String uuid : this.server.nets.keySet()) {
                    if (this.server.nets.get(uuid).equals(sock)) {
                        clientUUID = UUID.fromString(uuid);
                        break;
                    }
                }
                sock.receive(9);
                int buff = sock.receiveBuff.readInt();
                byte method = sock.receiveBuff.readByte();
                int length = sock.receiveBuff.readInt();
                if (method == 0) {
                    byte[] data = sock.receive(buff);
                    this.server.onMessage(new MessageEvent(new DataBuffer(data),NetworkSide.CLIENT,clientUUID.toString()));
                    for (Object o : this.server.messageHandler.keySet()) {
                        Method m = this.server.messageHandler.get(o);
                        m.invoke(o, new MessageEvent(new DataBuffer(data), NetworkSide.CLIENT));
                    }
                } else if (method == 1) {
                    byte[] data = sock.receive(buff);
                    for (String uuid : server.nets.keySet()) {
                        Net net = server.nets.get(uuid);
                        if (!uuid.equals(clientUUID.toString())) {
                            net.sendBuff.writeInt(data.length);
                            net.sendBuff.writeByte(1);
                            net.send();
                            net.send(data);
                        }
                    }
                } else if (method == 2) {
                    byte[] dest = sock.receive(length);
                    String destination = BufUtils.readString(sock.receiveBuff);
                    byte[] data = sock.receive(buff);
                    Net destinationClient = this.server.nets.get(destination);
                    destinationClient.sendBuff.writeInt(data.length);
                    destinationClient.sendBuff.writeByte(1);
                    destinationClient.send();
                    destinationClient.send(data);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}



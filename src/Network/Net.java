package Network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

public class Net {
    public Socket socket;
    BufferedInputStream input;
    BufferedOutputStream output;
    public ByteBuf sendBuff;
    public ByteBuf receiveBuff;
    int buffSize=1024;
    byte[] buff = new byte[1024];

    public Net(String ip, int port) throws Exception  {
        socket = new Socket(ip, port);
        input = new BufferedInputStream(socket.getInputStream());
        output = new BufferedOutputStream(socket.getOutputStream());
        sendBuff = Unpooled.buffer();
        receiveBuff = Unpooled.buffer();
    }

    public Net(Socket socket) throws Exception {
        this.socket = socket;
        input = new BufferedInputStream(socket.getInputStream());
        output = new BufferedOutputStream(socket.getOutputStream());
        sendBuff = Unpooled.buffer();
        receiveBuff = Unpooled.buffer();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }
    public void close() throws Exception {
        socket.close();
    }

    public int getPort(){
       return  socket.getPort();
    }

    public int getBuffSize() {
        return buffSize;
    }

    public void setBuffSize(int size) {
        buffSize=size;
        buff=new byte[size];
    }

    public byte[] receive() throws Exception {
        buff = new byte[buffSize];
        input.read(buff);
        receiveBuff = Unpooled.wrappedBuffer(buff);
        return buff;
    }

    public byte[] receive(int size) throws Exception {
        buff = new byte[size];
        input.read(buff);
        receiveBuff = Unpooled.wrappedBuffer(buff);
        return buff;
    }

    public void send() throws Exception {
        byte[] bytes = new byte[sendBuff.readableBytes()];
        int readerIndex = sendBuff.readerIndex();
        sendBuff.getBytes(readerIndex, bytes);
        output.write(bytes);
        output.flush();
        sendBuff = Unpooled.buffer();
    }

    public void send(ByteBuf buff) throws Exception {
        byte[] bytes = new byte[buff.readableBytes()];
        int readerIndex = buff.readerIndex();
        buff.getBytes(readerIndex, bytes);
        output.write(bytes);
        output.flush();
    }

    public void send(byte[] buff) throws  Exception {
        output.write(buff);
        output.flush();
    }

    public void send(DataBuffer buf) throws Exception {
        output.write(buf.data);
        output.flush();
    }

}

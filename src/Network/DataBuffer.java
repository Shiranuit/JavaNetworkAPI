package Network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public class DataBuffer {
    public final byte[] data;
    public final ByteBuf buf;
    public DataBuffer(byte[] data) {
        this.data = data;
        this.buf = Unpooled.wrappedBuffer(data);
    }

    public DataBuffer(ByteBuf buf) {
        this.buf = buf;
        this.data = ByteBufUtil.getBytes(buf);
    }
}

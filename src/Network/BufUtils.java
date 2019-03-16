package Network;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

public class BufUtils {
    public static void writeString(ByteBuf buf, String str) {
        buf.writeInt(str.length());
        for (int i=0; i<str.length(); i++) {
            buf.writeChar(str.charAt(i));
        }
    }

    public static String readString(ByteBuf buf) {
        int size = buf.readInt();
        String str = "";
        for (int i=0;i<size; i++) {
            str += buf.readChar();
        }
        return str;
    }
    
    public static String readUTF8String(ByteBuf from)
    {
        int len = from.readInt();
        String str = from.toString(from.readerIndex(), len, Charset.forName("UTF_8"));
        from.readerIndex(from.readerIndex() + len);
        return str;
    }
    
    public static void writeUTF8String(ByteBuf to, String string)
    {
        byte[] utf8Bytes = string.getBytes(Charset.forName("UTF_8"));
        to.writeInt(utf8Bytes.length);
        to.writeBytes(utf8Bytes);
    }

}

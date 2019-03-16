package Network;

public class MessageEvent {
    public final DataBuffer databuf;
    public final NetworkSide side;
    public final String uuid;
    public MessageEvent(DataBuffer databuf, NetworkSide side) {
        this.databuf=databuf;
        this.side=side;
        this.uuid="";
    }
    public MessageEvent(DataBuffer databuf) {
        this.databuf=databuf;
        this.side=NetworkSide.NULL;
        this.uuid="";
    }

    public MessageEvent(DataBuffer databuf, String uuid) {
        this.databuf=databuf;
        this.side=NetworkSide.NULL;
        this.uuid=uuid;
    }

    public MessageEvent(DataBuffer databuf, NetworkSide side, String uuid) {
        this.databuf=databuf;
        this.side=side;
        this.uuid=uuid;
    }
}

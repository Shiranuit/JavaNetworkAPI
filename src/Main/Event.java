package Main;

import Network.BufUtils;
import Network.MessageEvent;
import Network.NetworkEvent;
import Network.NetworkSide;

public class Event {
	Network.Client client;
	public Event(Network.Client client) {
		this.client = client;
	}
	
	@NetworkEvent(NetworkSide.CLIENT)
	public void onMessage(MessageEvent event) {
		//String pseudo = BufUtils.readString(event.databuf.buf);
		String txt = BufUtils.readString(event.databuf.buf);
		System.out.println(txt);
	}
	
	
}

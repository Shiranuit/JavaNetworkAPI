package Main;

import Network.Client;
import Network.Server;

public class Main {

	public static void main(String[] args) throws Exception {
			Server server = new Server(25565);
			server.start();
	}

}

package server;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stores a set of clients, represented by their ids.
 * 
 * @author Yohan Chalier
 *
 */
@SuppressWarnings("serial")
public class ClientPool extends HashMap<Integer, ConnectedClient> {
	
	public ConnectedClient findByMac(String MAC){
		for (Integer key: keySet()) {
			if (get(key).getMACAddress().equals(MAC))
				return get(key);
		}
		return null;
	}
	
	public int addClient(String MAC){
		int id = generateId();
		ConnectedClient client = new ConnectedClient(id);
		client.setMACAddress(MAC);
		put(id, client);
		return id;
	}
	
	private int generateId(){
		int id = ThreadLocalRandom.current().nextInt(1000, 10000);
		while (containsKey(id)) {
			id = ThreadLocalRandom.current().nextInt(1000, 10000);
		}
		return id;
	}
	
	public ConnectedClient identify(String idStr) {
		return identify(Integer.parseInt(idStr));
	}
	
	public ConnectedClient identify(int id) {
		return get(id);
	} 

}

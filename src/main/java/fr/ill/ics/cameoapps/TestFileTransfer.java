package fr.ill.ics.cameoapps;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.RemoteException;
import fr.ill.ics.cameo.Server;

public class TestFileTransfer {
	
	final static String BINARY = "binary";
	final static String TEXT = "text";
	
	public static void main(String[] args) {

		System.out.println("Test File transfer");
		
		Application.This.init(args);
		
		// The request message is the first argument.
		String type = args[0];
		String path = args[1];
			
		// Get the local Cameo server.
		Server server = Application.This.getServer();
		
		if (Application.This.isAvailable() && server.isAvailable()) {
			System.out.println("Connected server " + server);
		} else {
			System.exit(-1);
		}
		
		try {
			// Connect to the server.
			Application.Instance transferServer = server.connect("filetransfer");
			System.out.println("Application " + transferServer + " has state " + Application.State.toString(transferServer.getActualState()));
			
			// Create a requester.
			Application.Requester requester = Application.Requester.create(transferServer, "file-transfer");
			System.out.println("Created requester " + requester);
			
			JSONObject requestDataObject = new JSONObject();
	        requestDataObject.put("type", type);
	        requestDataObject.put("path", path);
			
			// Send a simple message as string.
			requester.send(requestDataObject.toJSONString());

			// Receive the response.
			if (type.equals(BINARY)) {
				byte[] response = requester.receive();
				System.out.println("File size " + response.length);
			}
			else if (type.equals(TEXT)) {
				String response = requester.receiveString();
				System.out.println("File content:\n" + response);
			}
				
			// Terminate the requester.
			requester.terminate();
			
		} catch (RemoteException e) {
			System.out.println("Requester error:" + e);
			
		} finally {
			// Do not forget to terminate This.
			Application.This.terminate();
		}
		
		System.out.println("finished the application");

	}
}

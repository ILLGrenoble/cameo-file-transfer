package fr.ill.ics.cameoapps;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.State;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.Requester;

public class TestFileTransfer {
	
	final static String BINARY = "binary";
	final static String TEXT = "text";
	final static String READ = "read";
	final static String WRITE = "write";
	final static String DELETE = "delete";
	
	public static void main(String[] args) {

		System.out.println("Test File transfer");
		
		This.init(args);
		
		// The request message is the first argument.
		String operation = args[0];
		String type = "";
    	String path = "";
    	String filePath = "";
			
		// Get the local Cameo server.
		Server server = This.getServer();
		
		try {
			// Connect to the server.
			App transferServer = server.connect("filetransfer");
			System.out.println("Application " + transferServer + " has state " + State.toString(transferServer.getActualState()));
			
			// Create a requester.
			Requester requester = Requester.create(transferServer, "file-transfer");
			requester.setTimeout(2000);
			requester.init();
			
			System.out.println("Created requester " + requester);

			///////////////////////////////////////////////////////////////////
	        if (operation.equals(WRITE)) {
	        	
	        	// Send a two parts message.
	        	type = args[1];
	        	path = args[2];
	        	filePath = args[3];

				JSONObject requestDataObject = new JSONObject();
				requestDataObject.put("operation", operation);
		        requestDataObject.put("type", type);
		        requestDataObject.put("path", path);
	        	
	        	if (type.equals(BINARY)) {
					// Read and reply the content.
					byte[] fileContent = Files.readAllBytes(FileSystems.getDefault().getPath(filePath));
					
					requester.sendTwoParts(requestDataObject.toJSONString().getBytes(), fileContent);
				}
				else if (type.equals(TEXT)) {
					
					String fileContent = "";
					List<String> fileLines = Files.readAllLines(FileSystems.getDefault().getPath(filePath));

					for (String line : fileLines) {
						fileContent += line + '\n';
					}
					
					requester.sendTwoParts(requestDataObject.toJSONString().getBytes(), fileContent.getBytes());
				}
	        }
	        else if (operation.equals(DELETE)) {
	        	
	        	path = args[1];
	        	
	        	JSONObject requestDataObject = new JSONObject();
				requestDataObject.put("operation", operation);
		        requestDataObject.put("path", path);
	        	
	        	// Send a one part message.
	        	requester.sendString(requestDataObject.toJSONString());
	        }
	        ///////////////////////////////////////////////////////////////////
	        else if (operation.equals(READ)) {
	        	
	        	type = args[1];
	        	path = args[2];
	        	
				JSONObject requestDataObject = new JSONObject();
				requestDataObject.put("operation", operation);
		        requestDataObject.put("type", type);
		        requestDataObject.put("path", path);
	        	
	        	// Send a one part message.
	        	requester.sendString(requestDataObject.toJSONString());
	        }

			if (operation.equals(READ)) {
				// Receive the response.
				if (type.equals(BINARY)) {
					byte[] response = requester.receive();
					System.out.println("File size " + response.length);
				}
				else if (type.equals(TEXT)) {
					String response = requester.receiveString();
					System.out.println("File content:\n" + response);
				}
			}
			else if (operation.equals(WRITE)) {
				// Receive the response.
				String response = requester.receiveString();
				System.out.println("Status " + response);
			}
			else if (operation.equals(DELETE)) {
				String response = requester.receiveString();
				System.out.println("Response: " + response);
			}
			else {
				System.out.println("Unknown operation");
			}
				
			// Terminate the requester.
			requester.terminate();
		}
		catch (Exception e) {
			System.out.println("Requester error:" + e);
		}
		finally {
			// Do not forget to terminate This.
			This.terminate();
		}

	}
}

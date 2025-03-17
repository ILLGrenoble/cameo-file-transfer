package eu.ill.cameo.filetransfer;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;

import org.json.simple.JSONObject;

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.State;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.coms.Requester;

public class FileTransferClient {
	
	final static String BINARY = "binary";
	final static String TEXT = "text";
	final static String DIRECTORY = "directory";
	final static String READ = "read";
	final static String WRITE = "write";
	final static String DELETE = "delete";
	final static String HELP = "help";
	final static String FILETRANSFER_SERVER_NAME = "filetransfer-server";

	private static void help() {
		
		System.out.println("Usage:");
		System.out.println("  help: prints this help");
		System.out.println("  " + READ + " <" + BINARY + " | " + TEXT + "> <remote file path>: reads the remote file located at path");
		System.out.println("  " + WRITE + " <" + BINARY + " | " + TEXT + "> <remote directory path> <local file path>: writes the local file located at path to the remote directory path");
		System.out.println("  " + DELETE + " <remote file path>: deletes the remote file located at path");
	}
	
	public static void main(String[] args) {

		// Check help.
		if (args.length <= 1 || args[0].equals(HELP)) {
			help();
			System.exit(1);
		}
		
		This.init(args);
		
		// The request message is the first argument.
		String operation = args[0];
		String type = "";
    	String path = "";
    	String filePath = "";
			
		// Get the Cameo server.
		Server server = This.getServer();
				
		try {
			// Connect to the server.
			App transferServer = server.connect(FILETRANSFER_SERVER_NAME);
			if (transferServer == null) {
				System.out.println("Cannot connect to the filetransfer app '" + FILETRANSFER_SERVER_NAME + "'");
				System.exit(1);
			}
						
			System.out.println("Application " + transferServer + " has state " + State.toString(transferServer.getState()));
			
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
				else if (type.equals(DIRECTORY)) {
					
					requester.send(requestDataObject.toJSONString().getBytes());
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

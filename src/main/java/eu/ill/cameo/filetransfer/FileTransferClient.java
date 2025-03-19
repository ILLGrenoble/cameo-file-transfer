package eu.ill.cameo.filetransfer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.json.simple.JSONObject;

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.Server;
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

	final static String OK = "OK";
	final static String ERROR = "Error";
	
	private static void help() {
		
		System.out.println("Usage:");
		System.out.println("  help: prints this help");
		System.out.println("  " + READ + " <" + BINARY + " | " + TEXT + "> <remote file path> <local file path>: reads the remote file located at path and copies to the local file path which can be a directory");
		System.out.println("  " + WRITE + " <" + BINARY + " | " + TEXT + "> <local file path> <remote file path>: writes the local file located at path to the remote file path");
		System.out.println("  " + WRITE + " " + DIRECTORY + " <remote directory path>: creates the remote directory and its parents located at path");
		System.out.println("  " + DELETE + " <remote file path>: deletes the remote file located at path");
	}
	
	private static void write(String[] args, Requester requester) {

    	String type = args[1];
    	String filePath = "";
    	String path = "";
    	
    	// Check number of arguments.
		if (type.equals(BINARY) || type.equals(TEXT)) {
			if (args.length < 5) {
				System.out.println("Bad number of arguments.");
				help();
				System.exit(1);
			}
			
			filePath = args[2];
	    	path = args[3];
		}
		else if (type.equals(DIRECTORY)) {
			if (args.length < 4) {
				System.out.println("Bad number of arguments.");
				help();
				System.exit(1);
			}
			
	    	path = args[2];
		}
    	
		JSONObject requestDataObject = new JSONObject();
		requestDataObject.put("operation", WRITE);
        requestDataObject.put("type", type);
        requestDataObject.put("path", path);
        
		try {
			if (type.equals(BINARY)) {
				
				if (args.length < 5) {
					System.out.println("Bad number of arguments.");
					help();
					System.exit(1);
				}
				
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
    	catch (IOException e) {
			System.out.println("Cannot read file: " + e.getMessage());
		}
    	
    	// Receive the response.
		String response = requester.receiveString();
		
		if (response.equals(OK)) {
    		System.out.println("File written successfully.");	
    	}
    	else {
    		System.out.println("File could not be written.");
    		System.exit(1);
    	}
	}
	
	private static void delete(String[] args, Requester requester) {
		
		if (args.length < 3) {
			System.out.println("Bad number of arguments.");
			help();
			System.exit(1);
		}
		
		String path = args[1];
    	
    	JSONObject requestDataObject = new JSONObject();
		requestDataObject.put("operation", DELETE);
        requestDataObject.put("path", path);
    	
    	// Send a one part message.
    	requester.sendString(requestDataObject.toJSONString());
    	
    	// Receive the response.
    	String response = requester.receiveString();
    	
    	if (response.equals(OK)) {
    		System.out.println("File deleted successfully.");	
    	}
    	else {
    		System.out.println("File could not be deleted.");
    		System.exit(1);
    	}
	}
	
	private static void writeLocalFile(String path, byte[] bytes) throws IOException {
		
		FileOutputStream outputStream;

		outputStream = new FileOutputStream(path);
		outputStream.write(bytes);
		outputStream.close();
	}
	
	private static void read(String[] args, Requester requester) throws IOException {
		
		if (args.length < 5) {
			System.out.println("Bad number of arguments.");
			help();
			System.exit(1);
		}
		
		String type = args[1];
		String path = args[2];
    	
		JSONObject requestDataObject = new JSONObject();
		requestDataObject.put("operation", READ);
        requestDataObject.put("type", type);
        requestDataObject.put("path", path);
    	
    	// Send a one part message.
    	requester.sendString(requestDataObject.toJSONString());
    	
    	// Prepare to write.
		Path filePath = Paths.get(path);
		String fileName = filePath.getFileName().toString();

		// Check if output path is a directory or not.
		Path outputPath = Paths.get(args[3]);
		Path outputFilePath;
		if (Files.isDirectory(outputPath)) {
			outputFilePath = Paths.get(args[3], fileName);	
		}
		else {
			outputFilePath = outputPath;
		}
    	
		// Create the directories if necessary.
		Path parentPath = outputFilePath.getParent();
		if (parentPath != null) {
			try {
				Files.createDirectories(parentPath);
			}
			catch (IOException e) {
				System.out.println("Cannot create directories: " + e.getMessage());
			}
		}
		
    	// Receive the response.
		if (type.equals(BINARY)) {
			byte[] response = requester.receive();
			System.out.println("File size " + response.length);
			
			writeLocalFile(outputFilePath.toString(), response);
		}
		else if (type.equals(TEXT)) {
			String response = requester.receiveString();
			System.out.println("File size " + response.getBytes().length);
			
			writeLocalFile(outputFilePath.toString(), response.getBytes());
		}
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

		// Get the Cameo server.
		Server server = This.getServer();
				
		try {
			// Connect to the server.
			App transferServer = server.connect(FILETRANSFER_SERVER_NAME);
			if (transferServer == null) {
				transferServer = server.start(FILETRANSFER_SERVER_NAME);
			}
						
			//System.out.println("Application " + transferServer + " has state " + State.toString(transferServer.getState()));
			
			// Create a requester.
			Requester requester = Requester.create(transferServer, "file-transfer");
			requester.setTimeout(2000);
			requester.init();
			
			//System.out.println("Created requester " + requester);

			// Check operation.
	        if (operation.equals(WRITE)) {
	        	write(args, requester);
	        }
	        else if (operation.equals(DELETE)) {
	        	delete(args, requester);
	        }
	        else if (operation.equals(READ)) {
	        	read(args, requester);
			}
			else {
				System.out.println("Unknown operation.");
			}
				
			// Terminate the requester.
			requester.terminate();
		}
		catch (Exception e) {
			System.out.println("Error:" + e);
		}
		finally {
			// Do not forget to terminate This.
			This.terminate();
		}
	}
}

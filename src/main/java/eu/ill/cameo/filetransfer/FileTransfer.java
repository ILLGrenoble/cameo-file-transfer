package eu.ill.cameo.filetransfer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.coms.basic.Request;
import eu.ill.cameo.api.coms.basic.Responder;

public class FileTransfer {

	final static String BINARY = "binary";
	final static String TEXT = "text";
	final static String DIRECTORY = "directory";
	final static String READ = "read";
	final static String WRITE = "write";
	final static String DELETE = "delete";
	
	final static String OK = "OK";
	final static String ERROR = "Error";
	
	private static void processRead(Request request, String type, String path) {
		
		try {
			if (type.equals(BINARY)) {
				// Read and reply the content.
				byte[] fileContent = Files.readAllBytes(FileSystems.getDefault().getPath(path));
				request.reply(fileContent);
			}
			else if (type.equals(TEXT)) {
				
				String fileContent = "";
				List<String> fileLines = Files.readAllLines(FileSystems.getDefault().getPath(path));
	
				for (String line : fileLines) {
					fileContent += line + '\n';
				}
				
				request.replyString(fileContent);
			}
			else {
				// Reply error.
				request.replyString("");
			}
		}
		catch (IOException e) {
			// Reply error.
			request.replyString("");
		}
	}
	
	private static void processWrite(Request request, String type, String path) {
		
		byte[] fileContent = request.getTwoParts()[1];
		if (fileContent != null) {
		
			// Create the directories if necessary.
			Path filePath = Paths.get(path);
			
			// Create the directories.
			Path parentPath = filePath.getParent();
			
			if (parentPath != null) {
				try {
					Files.createDirectories(parentPath);
				}
				catch (IOException e) {
					System.out.println("Cannot create directories: " + e.getMessage());
				}
			}
			
			// Check type.
			if (type.equals(BINARY)) {
				
				try {
					Files.write(filePath, fileContent);
					request.replyString(OK);	
				}
				catch (IOException e) {
					request.replyString(ERROR);
					System.out.println("Cannot write file: " + e.getMessage());
				}
			}
			else if (type.equals(TEXT)) {
				
				String textFileContent = new String(fileContent, Charset.forName("UTF-8"));
				
				try {
					Files.write(filePath, textFileContent.getBytes());
					request.replyString(OK);	
				}
				catch (IOException e) {
					request.replyString(ERROR);
					System.out.println("Cannot write file: " + e.getMessage());
				}
			}
		}
		else if (type.equals(DIRECTORY)) {
			
			try {
				Files.createDirectories(Paths.get(path));
				request.replyString(OK);
			}
			catch (IOException e) {
				request.replyString(ERROR);
				System.out.println("Cannot write file: " + e.getMessage());
			}
		}
	}
	
	private static void processDelete(Request request, String path) {
		
		// Delete the file if it exists.
		try {
			if (Files.deleteIfExists(FileSystems.getDefault().getPath(path))) {
				request.replyString(OK);	
			}
			else {
				request.replyString(ERROR);
			}
		}
		catch (IOException e) {
			request.replyString(ERROR);
		}
	}
	
	public static void main(String[] args) {
		
		This.init(args);
		
		// Create the JSON parser.
		JSONParser parser = new JSONParser();
				
		try {
			// Create the responder.
			Responder responder = Responder.create("file-transfer");
			responder.init();
			
			System.out.println("Created responder " + responder);
			
			// Set the state.
			This.setRunning();

			while (true) {
			
				// Receive the simple request.
				Request request = responder.receive();
	
				// Get and parse the data.
				String data = request.getString();
				String path = "";
				
				try {
					JSONObject requestParameters = (JSONObject)parser.parse(data);
			
					String operation = (String)requestParameters.get("operation");
					path = (String)requestParameters.get("path");
					
					///////////////////////////////////////////////////////////
					if (operation.equals(READ)) {
						processRead(request, (String)requestParameters.get("type"), path);
					}
					///////////////////////////////////////////////////////////
					else if (operation.equals(WRITE)) {
						processWrite(request, (String)requestParameters.get("type"), path);
					}
					///////////////////////////////////////////////////////////
					else if (operation.equals(DELETE)) {
						processDelete(request, path);
					}
				}
				catch (ParseException e) {
					System.err.println("Error while parsing data " + data);

					request.replyString(ERROR);
				}
			}
		}
		finally {
			// Do not forget to terminate This.
			This.terminate();			
		}
	}
}

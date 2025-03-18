package eu.ill.cameo.filetransfer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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
	
	public static void main(String[] args) {

		System.out.println("File transfer");
		
		This.init(args);
		
		if (This.isAvailable()) {
			System.out.println("connected");
		}
		
		// Create the JSON parser.
		JSONParser parser = new JSONParser();
				
		try {
			System.out.println("creating responder");
			
			// Create the responder.
			Responder responder = Responder.create("file-transfer");
			responder.init();
			
			System.out.println("created responder " + responder);
			
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
						
						String type = (String)requestParameters.get("type");
						
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
					///////////////////////////////////////////////////////////
					else if (operation.equals(WRITE)) {
						
						String type = (String)requestParameters.get("type");
						byte[] fileContent = request.getTwoParts()[1];
						if (fileContent != null) {
						
							if (type.equals(BINARY)) {
								
								try {
									Files.write(Paths.get(path), fileContent);
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
									Files.write(Paths.get(path), textFileContent.getBytes());
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
								Files.createDirectory(Paths.get(path));
								request.replyString(OK);
							}
							catch (IOException e) {
								request.replyString(ERROR);
								System.out.println("Cannot write file: " + e.getMessage());
							}
						}
					}
					///////////////////////////////////////////////////////////
					else if (operation.equals(DELETE)) {
						
						if (Files.deleteIfExists(FileSystems.getDefault().getPath(path))) {
							request.replyString(OK);	
						}
						else {
							request.replyString(ERROR);
						}
					}
				}
				catch (IOException e) {
					System.out.println("Cannot read file " + path);
					
					request.replyString(ERROR);
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

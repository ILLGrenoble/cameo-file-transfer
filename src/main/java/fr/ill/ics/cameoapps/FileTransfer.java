package fr.ill.ics.cameoapps;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.RemoteException;

public class FileTransfer {

	final static String BINARY = "binary";
	final static String TEXT = "text";
	
	public static void main(String[] args) {

		System.out.println("File transfer");
		
		Application.This.init(args);
		
		if (Application.This.isAvailable()) {
			System.out.println("connected");
		}
		
		// Create the JSON parser.
		JSONParser parser = new JSONParser();
				
		try {
			System.out.println("creating responder");
			
			// Create the responder.
			Application.Responder responder = Application.Responder.create("file-transfer");
			
			System.out.println("created responder " + responder);
			
			// Set the state.
			Application.This.setRunning();

			while (true) {
			
				// Receive the simple request.
				Application.Request request = responder.receive();
	
				// Get and parse the data.
				String data = request.get();
				String path = "";
				
				try {
					JSONObject requestParameters = (JSONObject)parser.parse(data);
			
					String type = (String)requestParameters.get("type");
					path = (String)requestParameters.get("path");
					
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
						
						request.reply(fileContent);
					}
					else {
						// Reply error.
						request.reply("");
					}
				}
				catch (IOException e) {
					System.out.println("Cannot read file " + path);
					
					request.reply("");
				}
				catch (ParseException e) {
					System.err.println("Error while parsing data " + data);

					request.reply("");
				}
				finally {
					// Terminate the request.
					request.terminate();
				}
			}
			
		}
		catch (RemoteException e) {
			System.out.println("responder error");
		}
		finally {
			// Do not forget to terminate This.
			Application.This.terminate();			
		}
		
		System.out.println("finished the application");

	}
}

package biomeConfigurationGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class YMLGenerator {
	
	private String fileName = "biomeConfig";
	
	private String chunkChance;
	
	public YMLGenerator(String chunkChance) {
		this.chunkChance = chunkChance;
	}
	
	/**
	 * Method to generate a file using the current YMLgenerator object
	 */
	public void generateFile()
	{
		//creates a null buffered writer object
		BufferedWriter writer = null;
		
		
		//tries to create and write a new YML file
		try 
		{
			//adds the ".yml" extension to the file name
			fileName = fileName + ".yml";
			
			//Creates a new file object with the file name
			File file = new File(fileName);
			
			//creates a blank file from the file object with the given file name
			file.createNewFile();
			
			//finds the exact path of the created file object to ensure writing to it is successful 
			String filePath = file.getCanonicalPath();
			
			//Instantiate the buffered writer object wrapped around a file writer object
			writer = new BufferedWriter(new FileWriter(filePath));
			
			//Writes individual attributes to the file with a line break at the end of each attribute IF they actually entered said attributes
			if(chunkChance != null)
			{
				writer.write("chunkChance: " + chunkChance);
				writer.newLine();
			}

		}
		catch (IOException e) 
		{
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				writer.close();
			} 
			catch (IOException e) 
			{
				System.err.println(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}
	
}

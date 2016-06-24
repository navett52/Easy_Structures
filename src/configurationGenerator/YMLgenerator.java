package configurationGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

/**
 * 
 * @author Connor
 *
 */
public class YMLgenerator 
{
	//variables to hold the individual attributes and file name
	private String fileName;
	private String attribute1;
	private String attribute2;
	private String attribute3;
	private String attribute4;
	private String attribute5;
	
	//no args constructor for object construction, will be removed later
	public YMLgenerator()
	{
		
	}
	
	/**
	 * Method to create a yml file using a given file name(will use other attributes to write data to the file in future)
	 * @param fileName = the given name of the file to be generated
	 */
	public void generateFile(String fileName, String attribute1, String attribute2 ,String attribute3 ,String attribute4 ,String attribute5)
	{
		this.fileName = fileName;
		BufferedWriter writer = null;
		
		this.attribute1 = attribute1;
		this.attribute2 = attribute2;
		this.attribute3 = attribute3;
		this.attribute4 = attribute4;
		this.attribute5 = attribute5;
		
		
		try 
		{
			fileName = fileName + ".yml";
			//Creates a new file object with the file name
			File file = new File(fileName);
			
			String filePath = file.getCanonicalPath();
			
			//creates a blank file from the file object with the given file name
			file.createNewFile();
			
			
			//writes attributes to the file
			writer = new BufferedWriter(new FileWriter(filePath));
			writer.write("Biome: " + attribute1);
			writer.newLine();
			writer.write("Spawn Chance: " + attribute2);
			writer.newLine();
			writer.write("Basement Depth: " + attribute3);
			writer.newLine();
			writer.write("Biome: " + attribute4);
			writer.newLine();
			writer.write("Biome: " + attribute5);
			
			
			
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
				e.printStackTrace();
			}
		}
	}
}

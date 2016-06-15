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
	public void generateFile(String fileName)
	{
		this.fileName = fileName;
		//Creates a new file object with the file name
		File file = new File(fileName);
		
		try 
		{
			//creates a blank file from the file object with the given file name
			file.createNewFile();
		} catch (IOException e) 
		{
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
}

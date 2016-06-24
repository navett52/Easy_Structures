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
	private String place;
	private String maxSpawns;
	private String chanceToSpawn;
	private String basementDepth;
	private String minHeight;
	private String maxHeight;
	private String randomRotation;
	private String pasteAir;
	//no args constructor for object construction, will be removed later
	public YMLgenerator()
	{
		
	}
	
	/**
	 * Method to create a yml file using a given file name(will use other attributes to write data to the file in future)
	 * @param fileName = the given name of the file to be generated
	 */
	public void generateFile(String fileName, String place, String maxSpawns ,String chanceToSpawn ,String basementDepth ,String minHeight, 
			String maxHeight, String randomRotation, String pasteAir)
	{
		this.fileName = fileName;
		BufferedWriter writer = null;
		
		this.place = place;
		this.maxSpawns = maxSpawns;
		this.chanceToSpawn = chanceToSpawn;
		this.basementDepth = basementDepth;
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		this.randomRotation = randomRotation;
		this.pasteAir = pasteAir;
		
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
			writer.write("place: " + place);
			writer.newLine();
			writer.write("maxspawns: " + maxSpawns);
			writer.newLine();
			writer.write("chance: " + chanceToSpawn);
			writer.newLine();
			writer.write("basementdepth: " + basementDepth);
			writer.newLine();
			writer.write("anywhereminY: " + minHeight);
			writer.newLine();
			writer.write("anywheremaxY: " + maxHeight);
			writer.newLine();
			writer.write("randomrotate: " + randomRotation);
			writer.newLine();
			writer.write("pasteschematicair: " + pasteAir);
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

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
	
	
	/**
	 * Constructor for YMLgenerator object
	 * @param fileName = Name of the file to be generated
	 * @param place = place the schematic will be pasted
	 * @param maxSpawns = maximum spawns of a schematic
	 * @param chanceToSpawn = variable that determines the percent chance of a schematic randomly spawning
	 * @param basementDepth = the offset of a schematic to the blocks it's pasted on
	 * @param minHeight = the lowest height that a schematic can be spawned
	 * @param maxHeight = the highest height that a schematic can be spawned
	 * @param randomRotation = determines whether or not you want a schematic randomly rotated when spawning
	 * @param pasteAir = determines whether or not you want a schematic pasted with air blocks included
	 */
	public YMLgenerator(String fileName, String place, String maxSpawns ,String chanceToSpawn ,String basementDepth ,String minHeight, String maxHeight, String randomRotation, String pasteAir)
	{
		//Sets the class level variables to equal the construction values
		this.fileName = fileName;
		this.place = place;
		this.maxSpawns = maxSpawns;
		this.chanceToSpawn = chanceToSpawn;
		this.basementDepth = basementDepth;
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		this.randomRotation = randomRotation;
		this.pasteAir = pasteAir;
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
			if(place != null)
			{
				writer.write("place: " + place);
				writer.newLine();
			}
			if(maxSpawns != null)
			{
				writer.write("maxspawns: " + maxSpawns);
				writer.newLine();
			}
			if(chanceToSpawn != null)
			{
				writer.write("chance: " + chanceToSpawn);
				writer.newLine();
			}
			if(basementDepth != null)
			{
				writer.write("basementdepth: " + basementDepth);
				writer.newLine();
			}
			if(minHeight != null)
			{
				writer.write("anywhereminY: " + minHeight);
				writer.newLine();
			}
			if(maxHeight != null)
			{
				writer.write("anywheremaxY: " + maxHeight);
				writer.newLine();
			}
			if(randomRotation != null)
			{
				writer.write("randomrotate: " + randomRotation);
				writer.newLine();
			}
			if(pasteAir != null)
			{
				writer.write("pasteschematicair: " + pasteAir);
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

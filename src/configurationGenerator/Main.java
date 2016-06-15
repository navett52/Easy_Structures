package configurationGenerator;

public class Main 
{

	public static void main(String[] args) 
	{
		//creates a YMLgenerator object
		YMLgenerator yml = new YMLgenerator();
		
		//calls the method to create a blank yml file with a given name
		yml.generateFile("Schematic.yml");
	}

}
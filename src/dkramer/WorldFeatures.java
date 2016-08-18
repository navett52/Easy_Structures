/*
 * Easy Structures Minecraft plugin main class
 * Coded by __________ development team
 * Credit for template code to dkramer https://github.com/robotnikthingy/WorldSchematics
 */

package dkramer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldFeatures extends JavaPlugin {
	//Creating a public static instance of this class
	public static WorldFeatures instance;
	//Declaring the logger to send info to the console
    public static Logger log;
    //Creating a hashmap to hold all of the config files that need to be accessed
    private static final HashMap<String, BetterConfiguration> configs = new HashMap<String, BetterConfiguration>();
    //Declaring a chunk listener
    public ChunkListener c1;
    //Declaring a player listener
    public PlayerListener p1;
    
    /**
     * Runs when the plugin is disabled
     */
    public void onDisable() {
    	//States to the console that this plugin has been disabled
        log.info("Disabled");
    }
    
    /**
     * Runs when the plugin is enabled
     */
    public void onEnable() {
    	//Grabbing the plugin description file
		PluginDescriptionFile pdfFile = getDescription();
		//Instantiating the logger on startup of the plugin
    	log = getLogger();
    	//Instantiating instance to equal this current instance of the plugin
    	instance = this;
    	//Simply sending a message to the console letting them know what version of the plugin they have
		log.info(pdfFile.getName() + " has been enabled (V." + pdfFile.getVersion() + ")");
		//Creating the folder structure for the plugin
        createFolders();
        //Making this instance aware of the new instances of c1 and p1
        //and making c1 and p1 aware of this specific instance of this class
        c1 = new ChunkListener(this);
        p1 = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(new ChunkListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        //Setting a scheduled event to save config files
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				saveAllConfigs();
			}
        }, 20 * 60 * 15, 20 * 60 * 15);
        this.saveDefaultConfig();
    }
    
    /**
	 * Runs when a command (anything with a / before it) is run in the Console or MC Chat
	 * sender: Whatever issues the command (ex. Player, Console, etc..)
	 * command: The command that needs to be entered
	 * label: The string entered into console or chat (ex. /a command goes here)
	 * args: String array to hold arguments for a command (ex. /set <- the command /set |color purple| <- args... i think)
	 * @author Evan Tellep
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//A command to allow world creators to regenerate chunks that have been generated without schematics in the appropriate folders
		//Will most likely crash the server, but the chunks do seem to regenerate
		if (command.getName().equalsIgnoreCase("repopulate")) {
			//Currently this command may only be ran by a player, but I might be able to change that
			if (sender instanceof Player) {
				//Casting sender to player since the previous if statement makes sure it is
				Player player = (Player) sender;
				//Grabbing all the chunk files within the specific folder of the world the player is in
				String[] chunkFiles = new File("plugins/Easy_Structures/PopulatedChunks/" + "/" + player.getWorld().getName()).list();
				//For every chunk file, checks the property of populated
				for (int i = 0; i < chunkFiles.length; i ++) {
					//grabbing a specific file
					File chunkFile = new File("plugins/Easy_Structures/PopulatedChunks/" + "/" + player.getWorld().getName() + "/" + chunkFiles[i]);
					//Attempting to use a buffer reader to read the line
					try {
						//Instantiating a buffered reader using a file reader being instantiated with the grabbed file
						BufferedReader br = new BufferedReader(new FileReader(chunkFile));
						//Reading the first (and only) line of the file
						String line = br.readLine();
						//Converting the String value into a boolean
						boolean beenPopulated = Boolean.parseBoolean(line.substring(11));
						//If the value is false, meaning the chunk has not yet tried to be populated using the plugin it regenerates the chunk
						if (beenPopulated == false) {
							//Grabbing the X coordinate of the chunk from the name of the file
							int chunkX = Integer.parseInt(chunkFiles[i].substring(chunkFiles[i].indexOf('_') + 1, chunkFiles[i].indexOf(',')));
							//Grabbing the Z coordinate of the chunk from the name of the file
							int chunkZ = Integer.parseInt(chunkFiles[i].substring(chunkFiles[i].indexOf(',') + 1, chunkFiles[i].indexOf('.')));
							//Using player to get the world to regen the chunk, but I should be able to throw in an arg to allow someone from console
							//to specify a world to re-populate
							player.getWorld().regenerateChunk(chunkX, chunkZ);
						}
						//Attemtpting to close the buffered reader
						br.close();
					} catch (IOException e) {
						//If the error is thrown catch it here and print the localized message and it's stack trace
						System.out.println(e.getLocalizedMessage());
						e.printStackTrace();
					}
				}
			}
			//If the sender is not an instance of player let them know.
			System.out.println("You must be a player to use this command!");
		}
		
		if (command.getName().equalsIgnoreCase("notrees")) {
			Player player = (Player) sender;
			player.sendMessage(ChatColor.GOLD + "No trees command has been issued");
			Chunk chunk = player.getLocation().getChunk();
			int chunkX = chunk.getX() * 16;
			int chunkZ = chunk.getZ() * 16;
			player.sendMessage("You are in chunk: " + chunkX + "," + player.getWorld().getMaxHeight() + "," + chunkZ);
			c1.noDefaultTrees(chunkX, player.getWorld().getMaxHeight() -1, chunkZ);
		}
		
		//An Attempt to make a command to allow server admin to create folders for a world
		if (command.getName().equalsIgnoreCase("makefolders")) {
			c1.genFolders(args[0]);
			return true;
		} else {
			return false;
		}
	}
    
	/**
	 * Saves the configs
	 */
    private void saveAllConfigs() {
    	//log.info("Saving Information...");
    	for(BetterConfiguration config : configs.values()) {
        	config.save();
        }
    	//log.info("Done Saving!");
    }
    
    /**
     * GRabs a file and adds it to the config hashmap
     * @param path The path of the file to become a BetterConfiguration object
     * @return The file which is now a BetterConfiguration object
     */
    public static BetterConfiguration getConfig(String path) {
    	if(!configs.containsKey(path)) {
    		BetterConfiguration config = new BetterConfiguration(path + ".yml");
    		config.load();
    		configs.put(path, config);
    	}
    	return configs.get(path);
    }
    
    /**
     * Get's a list of the worlds 
     * @return String[] holding the names of the worlds
     * @author Evan Tellep
     */
	public ArrayList<String> getWorldFolders() {
		//Create the ArrayList to be returned later
		ArrayList<String> Worlds = new ArrayList<String>();
		//Grabbing the current file path the plugin JAR is in to look at the files inside it
		File serverPath = new File(System.getProperty("user.dir"));
		//Transferring the names of the files/directories in the current path to a String[]
		String[] serverFiles = serverPath.list();
		/*
		 * Cycles through all files/directories and if one is a directory looks inside for a level.dat
		 * file since every world has one.
		 */
		for (int i = 0; i < serverFiles.length; i++) {
			//tacking the file/directory names from the serverFiles String[] to the server path
			File FileOrFolder = new File(serverPath + "/" + serverFiles[i]);
			//Checking if something is a file/directory
			if (FileOrFolder.isDirectory()) {
				//If it is a directory, grab the names of all the files/directories inside of it
				String[] FolderList = FileOrFolder.list();
				//Cycles through all files/directories inside the directory we just found
				for (int j = 0; j < FolderList.length; j++) {
					//Checks to see if the current Directory we are in holds a level.dat file
					if (FolderList[j].compareTo("level.dat") == 0) {
						//If there is a level.dat then it is a world folder so we add the name of the folder to the Worlds[]
						Worlds.add(serverFiles[i]);
					}
				}
			}
		}
		return Worlds;
	}
    
	/**
	 * Creates folders to hold schematics to be generated into the world
	 * @author Evan Tellep
	 */
	public static void createFolders() {
		//Creating a variable to count the number of biome folders to make sure all are getting created
		int BiomeFolderCount = 0;
		//Creating a variable to count the number of world folders to make sure all are getting created
		int WorldFolderCount = 0;
		//Creating an ArrayList<> to hold the Biome ENUM values to make sure all biome folders are named appropriately and all biomes are accounted for
		//Using ArrayList<> so if MC updates and adds new biomes we don't have to worry about updating this.
		ArrayList<Biome> biomes = new ArrayList<Biome>(Arrays.asList(Biome.values()));
		//Creating an ArrayList<> to hold all the world names
		ArrayList<String> worlds = new ArrayList<String>(instance.getWorldFolders());
		for (int i = 0; i < worlds.size(); i++) {
			File chunkFolder = new File("plugins/Easy_Structures/PopulatedChunks/" + "/" + worlds.get(i));
			chunkFolder.mkdirs();
		}
		/*
		 * Creates the file structure locally so the Structure file will be in the same location as the server file.
		 * When making the generator method we can seek the file paths locally.
		 * Also cycles through the Biome[] and uses it to name all Biome folders
		 */
		for (int i = 0; i < worlds.size(); i++) {
			//Creating each world folder
			File dirInit = new File("plugins/Easy_Structures/Schematics/" + "/" + worlds.get(i));
			//Creating each directory
			boolean makeDirInit = dirInit.mkdirs();
			//Checking to see if it was created successfully
			if (makeDirInit) {
				//Add to World Folder Count
				WorldFolderCount++;
			}
			//Creating the Biomes folders inside of the worlds folders
			for (int j = 0; j < biomes.size(); j++) {
				//Declaring and instantiating a File directory object with the path we want
				File dirBiomes = new File("plugins/Easy_Structures/Schematics/" + "/" + worlds.get(i) + "/" + biomes.get(j));
				//Creating the directories with mkdirs() method. returns true if successful
				boolean makeDirBiomes = dirBiomes.mkdirs();
				//If the making of a biome folder was successful increment BiomeFolderCount
				if (makeDirBiomes) {
					BiomeFolderCount++;
				}
			}
		}
		//Sees how many folders were created
		if (BiomeFolderCount == biomes.size() * worlds.size() && WorldFolderCount == worlds.size()) {
			//If all Biome folders were made (if it counts the same amount of folders as the size of the arraylist)
			log.info("All folders Created successfully. Probably your fisrt time running this plugin.");
			log.info("You can now put schematics in the appropriate folders to be generated.");
		} else if (BiomeFolderCount < biomes.size() * worlds.size() && BiomeFolderCount > 0 || WorldFolderCount < worlds.size() && WorldFolderCount > 0) {
			//If at least 1 folder was made
			log.info("Some folders Created successfully. Minecraft probably added some new Biomes and the list is updating or you added more worlds to your server.");
			log.info("You can now put schematics in the appropriate folder for the new world or biome to be generated.");
		} else {
			//if no folders were made
			log.info("No folders generated. The folders might have already been made.");
		}
	}
}
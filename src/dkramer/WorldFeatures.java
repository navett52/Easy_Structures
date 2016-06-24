package dkramer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

public class WorldFeatures extends JavaPlugin {
	
	private static WorldFeatures plugin;
	
	public static WorldFeatures instance;
    public static Logger log;
    private static final HashMap<Player, PlayerInfo> playerInfos = new HashMap<Player, PlayerInfo>();
    private static final HashMap<String, BetterConfiguration> configs = new HashMap<String, BetterConfiguration>();

    public ChunkListener c1;
    public PlayerListener p1;
    
    
    public static String defaultCuboidPicker = "SEEDS";
        
    public void onDisable() {
        log.info("Disabled");
    }

    public void onEnable() {
    	log = getLogger();
    	instance = this;
    	
    	//Nave's Additions
        createFolders();
    	//End Nave's Additions
    	
        //Confusing stuff to pass this to that to this and back
        c1 = new ChunkListener(this);
        p1 = new PlayerListener(this);

        getServer().getPluginManager().registerEvents(new ChunkListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				saveAllConfigs();
			}
        }, 20 * 60 * 15, 20 * 60 * 15);
        this.saveDefaultConfig();
    }
    
    private void saveAllConfigs() {
    	//log.info("Saving Information...");
    	for(BetterConfiguration config : configs.values()) {
        	config.save();
        }
    	//log.info("Done Saving!");
    }
    
    public static BetterConfiguration getConfig(String path) {
    	if(!configs.containsKey(path)) {
    		BetterConfiguration config = new BetterConfiguration(path + ".yml");
    		config.load();
    		configs.put(path, config);
    	}
    	return configs.get(path);
    }
    
    
    public static PlayerInfo getPlayerInfo(Player player) {
    	if(!playerInfos.containsKey(player)) {
    		playerInfos.put(player, new PlayerInfo());
    	}
    	return playerInfos.get(player);
    }

	private void saveArea(World world, Vector origin, Vector size, File file) {
        EditSession es = new EditSession(new BukkitWorld(world), 0x30d40);
        CuboidClipboard cc = new CuboidClipboard(origin, size);
        cc.copy(es);
        try {
        	MCEditSchematicFormat.MCEDIT.save(cc, file);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String getErrorMessage(boolean left) {
    	String error = (new StringBuilder())
    			.append(ChatColor.YELLOW)
    			.append("You need a corner! To select it, wield ")
    			.append(this.getConfig().getString("wandmaterial").toLowerCase().replaceAll("_", " ")).toString();
    	error += left ? " and left click." : " and right click.";
    	return error;
    }
    
    //Nave
    public static List<World> getWorlds() {
    	return plugin.getServer().getWorlds();
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
	 * @return 0 if every folder has been created
	 * @return 1 if some folders have been created
	 * @return 2
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
	
		//**********************DEBUGGING**********************\\
		
		    //List<World> worlds = new ArrayList<World>(getWorlds());
		    //log.info(worlds.size());
		
		    //ArrayList<WorldServer> worlds = new ArrayList<WorldServer>(MinecraftServer.getServer().worldServer);
		    //log.info(worlds.size());
		    //ArrayList<MultiverseWorld> mvworlds = new ArrayList<MultiverseWorld>(worldmanager.getMVWorlds());
		    //log.info(mvworlds.isEmpty());
		    //CraftServer server1 = new CraftServer(server, server.getPlayerList());
		    //ArrayList<World> worlds2 = (ArrayList<World>) server1.getWorlds();
		    //log.info(worlds2.size()); //prints 0
			/*
			for (World w1 : Bukkit.getWorlds()) {
				log.info(w1.getName());//prints null
			}
			
			log.info(Bukkit.getName());//prints server name (CraftBukkit i think)
			
			log.info(Bukkit.getServer().getName());//prints server name (CraftBukkit i think)
			
			log.info(Bukkit.getWorld("world"));//prints null
			
			for (World worlds1 : Bukkit.getWorlds()) {
				log.info(worlds1.getName()); //prints nothing
			}
			
			for (int i = 0; i < biomes.size(); i++) {
				log.info(biomes.get(i));  //Successful: Prints all biome names; un-needed
			}
			
			for (int i = 0; i < worlds.size(); i++) {
				log.info(worlds.get(i));//prints nothing
			}
			
			log.info(worlds.size());//prints 0
			
			log.info(Bukkit.getWorlds().toString());//gives [] 
			
			log.info(Bukkit.getWorlds().size());//prints 0
			
			log.info(Bukkit.getWorlds().size());//prints 0
			
			//Failed attempt at using Multiverse
			MultiverseCore mvc = new MultiverseCore();
			Collection<MultiverseWorld> mvw = new ArrayList<MultiverseWorld>();
			mvw = mvc.getMVWorldManager().getMVWorlds();
			log.info(mvw.size());
			MultiverseWorld[] mvwa = new MultiverseWorld[mvw.size()];
			mvw.toArray(mvwa);
			log.info(mvwa.length);
			for (int i = 0; i < mvwa.length; i++) {
				log.info(mvwa[i]);
			}
			*/
		//**********************DEBUGGING**********************\\
		
		
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
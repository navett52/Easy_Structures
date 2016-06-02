package dkramer;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.block.Biome;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.api.*;
import com.google.common.io.Files;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

public class WorldFeatures extends JavaPlugin {
	public static WorldFeatures instance;

    public static final Logger logger = Logger.getLogger("Minecraft");
    private static final HashMap<Player, PlayerInfo> playerInfos = new HashMap<Player, PlayerInfo>();
    private static final HashMap<String, BetterConfiguration> configs = new HashMap<String, BetterConfiguration>();

    public ChunkListener c1;
    public PlayerListener p1;
    
    
    public static String defaultCuboidPicker = "SEEDS";
        
    public void onDisable() {
        logger.info("Easy_Structures Disabled");
    }

    public void onEnable() {
    	instance = this;
        createFolders();
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
    	System.out.println("[Easy_Structures] Saving Information...");
    	for(BetterConfiguration config : configs.values()) {
        	config.save();
        }
    	System.out.println("[Easy_Structures] Done Saving!");
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
    
	/**
	 * Creates folders to hold schematics to be generated into the world
	 * @return 0 if every folder has been created
	 * @return 1 if some folders have been created
	 * @return 2
	 */
	public static void createFolders() {
		//Creating a variable to count the number of biome folders to make sure all are getting created
		int BiomeFolderCount = 0;
		
		int WorldFolderCount = 0;
		//Creating an ArrayList<> to hold the Biome ENUM values to make sure all biome folders are named appropriately and all biomes are accounted for
		//Using ArrayList<> so if MC updates and adds new biomes we don't have to worry about updating this.
		ArrayList<Biome> biomes = new ArrayList<Biome>(Arrays.asList(Biome.values()));
		List worlds = Bukkit.getServer().getWorlds();
		
		/**
		 * Creates the file structure locally so the Structure file will be in the same location as the server file.
		 * When making the generator method we can seek the file paths locally.
		 * Also cycles through the Biome[] and uses it to name all Biome folders
		 */
		for (int i = 0; i < worlds.size(); i++) {
			File dirInit = new File("plugins/Easy_Structures/Schematics/" + worlds.get(i));
			boolean makeDirInit = dirInit.mkdirs();
			for (int j = 0; j < biomes.size(); j++) {
				//Declaring and instantiating a File directory object with the path we want
				File dirBiomes = new File("plugins/Easy_Structures/Schematics/" + worlds.get(i) + "/" + biomes.get(i));
				//Creating the directories with mkdirs() method. returns true if successful
				boolean makeDirBiomes = dirBiomes.mkdirs();
				//If the making of a biome folder was successful increment BiomeFolderCount
				if (makeDirBiomes) {
					BiomeFolderCount++;
				}
			}
			if (makeDirInit) {
				WorldFolderCount++;
			}
		}
		//Sees how many folders were created
		if (BiomeFolderCount == biomes.size() && WorldFolderCount == worlds.size()) {
			//If all Biome folders were made (if it counts the same amount of folders as the size of the arraylist)
			logger.info(ChatColor.GREEN + "[Easy Structures] All folders Created successfully. Probably your fisrt time running this plugin.");
			logger.info(ChatColor.GREEN + "[Easy Structures] You can now put schematics in the appropriate folders to be generated.");
		} else if (BiomeFolderCount < biomes.size() && BiomeFolderCount > 0 || WorldFolderCount < worlds.size() && WorldFolderCount > 0) {
			//If at least 1 folder was made
			logger.info(ChatColor.YELLOW + "[Easy Structures] Some folders Created successfully. Minecraft probably added some new Biomes and the list is updating or you added more worlds to your server.");
			logger.info(ChatColor.YELLOW + "[Easy Structures] You can now put schematics in the appropriate folder for the new world or biome to be generated.");
		} else {
			//if no folders were made
			logger.info(ChatColor.RED + "[Easy Structures] The folders have already been made.");
		}
	}
    
 
}
package dkramer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

import net.minecraft.server.v1_9_R2.MinecraftServer;
import net.minecraft.server.v1_9_R2.PlayerList;

public class WorldFeatures extends JavaPlugin {
	public static WorldFeatures instance;

    public static Logger log;
    private static final HashMap<Player, PlayerInfo> playerInfos = new HashMap<Player, PlayerInfo>();
    private static final HashMap<String, BetterConfiguration> configs = new HashMap<String, BetterConfiguration>();

    public ChunkListener c1;
    public PlayerListener p1;
    
    
    public static String defaultCuboidPicker = "SEEDS";
        
    public void onDisable() {
        log.info("Easy_Structures Disabled");
    }

    public void onEnable() {
    	log = getLogger();
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
		
		CraftServer server = (CraftServer) Bukkit.getServer();
		//Creating an ArrayList<> to hold the Biome ENUM values to make sure all biome folders are named appropriately and all biomes are accounted for
		//Using ArrayList<> so if MC updates and adds new biomes we don't have to worry about updating this.
		ArrayList<Biome> biomes = new ArrayList<Biome>(Arrays.asList(Biome.values()));
		ArrayList<World> worlds = new ArrayList<World>(server.getWorlds());
		
		
		//**DEBUGGING**\\
		CraftServer server1 = (CraftServer) Bukkit.getServer();
		ArrayList<World> worlds2 = (ArrayList<World>) server1.getWorlds();
		System.out.println(worlds2.size()); //prints 0
		
		for (World w1 : Bukkit.getWorlds()) {
			System.out.println(w1.getName());//prints null
		}
		
		System.out.println(Bukkit.getName());//prints server name (CraftBukkit i think)
		
		System.out.println(Bukkit.getServer().getName());//prints server name (CraftBukkit i think)
		
		System.out.println(Bukkit.getWorld("world"));//prints null
		
		for (World worlds1 : Bukkit.getWorlds()) {
			System.out.println(worlds1.getName()); //prints nothing
		}
		
		/*for (int i = 0; i < biomes.size(); i++) {
			System.out.println(biomes.get(i));  //Successful: Prints all biome names; un-needed
		} */
		
		for (int i = 0; i < worlds.size(); i++) {
			System.out.println(worlds.get(i));//prints nothing
		}
		
		System.out.println(worlds.size());//prints 0
		
		log.info(Bukkit.getWorlds().toString());//gives [] 
		
		System.out.println(Bukkit.getWorlds().size());//prints 0
		
		System.out.println(Bukkit.getWorlds().size());//prints 0
		
		//Failed attempt at using Multiverse
		/*MultiverseCore mvc = new MultiverseCore();
		Collection<MultiverseWorld> mvw = new ArrayList<MultiverseWorld>();
		mvw = mvc.getMVWorldManager().getMVWorlds();
		System.out.println(mvw.size());
		MultiverseWorld[] mvwa = new MultiverseWorld[mvw.size()];
		mvw.toArray(mvwa);
		System.out.println(mvwa.length);
		for (int i = 0; i < mvwa.length; i++) {
			System.out.println(mvwa[i]);
		}*/
		
		//**DEBUGGING**\\
		
		
		/**
		 * Creates the file structure locally so the Structure file will be in the same location as the server file.
		 * When making the generator method we can seek the file paths locally.
		 * Also cycles through the Biome[] and uses it to name all Biome folders
		 */
		for (int i = 0; i < worlds.size(); i++) {
			File dirInit = new File("plugins/Easy_Structures/Schematics/" + worlds.get(i).getName());
			boolean makeDirInit = dirInit.mkdirs();
			if (makeDirInit) {
				WorldFolderCount++;
			}
			for (int j = 0; j < biomes.size(); j++) {
				//Declaring and instantiating a File directory object with the path we want
				File dirBiomes = new File("plugins/Easy_Structures/Schematics/" + worlds.get(i) + "/" + biomes.get(j));
				//Creating the directories with mkdirs() method. returns true if successful
				boolean makeDirBiomes = dirBiomes.mkdirs();
				//If the making of a biome folder was successful increment BiomeFolderCount
				if (makeDirBiomes) {
					BiomeFolderCount++;
				}
			}
		}
		//Sees how many folders were created
		if (BiomeFolderCount == biomes.size() && WorldFolderCount == worlds.size()) {
			//If all Biome folders were made (if it counts the same amount of folders as the size of the arraylist)
			log.fine("[Easy Structures] All folders Created successfully. Probably your fisrt time running this plugin.");
			log.fine("[Easy Structures] You can now put schematics in the appropriate folders to be generated.");
		} else if (BiomeFolderCount < biomes.size() && BiomeFolderCount > 0 || WorldFolderCount < worlds.size() && WorldFolderCount > 0) {
			//If at least 1 folder was made
			log.warning("[Easy Structures] Some folders Created successfully. Minecraft probably added some new Biomes and the list is updating or you added more worlds to your server.");
			log.warning("[Easy Structures] You can now put schematics in the appropriate folder for the new world or biome to be generated.");
		} else {
			//if no folders were made
			log.severe("[Easy Structures] No folders generated. The folders might have already been made.");
		}
	}
    
 
}
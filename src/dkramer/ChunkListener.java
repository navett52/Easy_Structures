package dkramer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.generator.BlockPopulator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

public class ChunkListener implements Listener{
	//A random number generator
	Random rand;
	//The chunk being populated
    private Chunk chunk;
    //Height of the schematics base
    private int baseHeight;
    //The world the chunk is being loaded in
    private World world;
    //The chunk's X coord
    private int chunkX;
    //the chunk's Z coord
    private int chunkZ;
    //A random X coord in a chunk
    private int randX;
    //A random Z coord in a chunk
    private int randZ;
    //The width of the schematic to be placed
    private int width;
    //The length of the schematic to be placed
    private int length;
    //The height of the schematic to be placed
    private int height;
    //How many degrees the schematic will rotate if randomRotation = true
    private int rotation;
    //A reference point to worldFeatures class
    public static WorldFeatures plugin = WorldFeatures.instance;
    //Declaring a world edit cuboid clipboard to be used 
    CuboidClipboard cc;
    
    
    
    /**
     * The method where all the magic happens.
     * @param event I think event handler takes care of this?
     */
	@EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {
		//Getting the chunk that is triggering the event
    	chunk = event.getChunk();
        chunkX = chunk.getX() * 16;
        chunkZ = chunk.getZ() * 16;
        world = chunk.getWorld();
        //Getting the random coords within the chunk
        randX = rand.nextInt(16);
        randZ = rand.nextInt(16);
        //Getting the maxHeight of the world
        int maxHeight = world.getMaxHeight() - 1;
        //Creating a vector to set the offest of the schematic to 0 that way the schematic doesn't paste from where it was copied from like how it normally does
        Vector offSet = new Vector(0, 0, 0);
        
        //noDefaultTrees(chunkX, maxHeight, chunkZ);
//////////////////////////////Getting the world schematics/////////////////////////////////////////// 
        //Creating the file path to be looked into for schematic files to be generated in the world
        String worldPath = "plugins/Easy_Structures/Schematics/" + "/" + world.getName();
        //Setting the path to look in the biome folder of the biome the block is currently in
        String biomePath = "plugins/Easy_Structures/Schematics/" + "/" + loadBlockInChunk(randX, baseHeight, randZ).getBiome().toString();
        
        //Creating arraylist to hold the schematic files from the world folder
		ArrayList<String> schematicsForWorldGen = new ArrayList<String>();
        //Instantiating an arraylist to hold the schematics that are inside the biome folder
		ArrayList<String> schematicsForBiomeGen = new ArrayList<String>();
		
		//Getting all files within the worldPath
		String[] filesInWorldFolder = new File(worldPath).list();
		//Grabbing all files that are inside the biome folder
        String[] filesInBiomeFolder = new File(biomePath).list();
		
        //Automagically generating the file paths for the world if it is new.
		//Unfortunately it does not automagically populate the folders with schematics
        genFolders(worldPath);  
        
        //Looks at the list of files in the worldPath and grabs all schematic files
        schematicsForWorldGen = schematicsForGen(filesInWorldFolder);
        schematicsForBiomeGen = schematicsForGen(filesInBiomeFolder);
        
        /*Debug Statement*/
        if (schematicsForWorldGen.isEmpty() && plugin.getConfig().getBoolean("debug") == true) {
        	WorldFeatures.log.warning("Did not find any schematics in world folder: " + world.getName() + "!");
        } else if (schematicsForBiomeGen.isEmpty() && plugin.getConfig().getBoolean("debug") == true) {
        	WorldFeatures.log.warning("Did not find any schematics in biome folder: " + loadBlockInChunk(randX, baseHeight, randZ).getBiome().toString() + "!");
        }
        /*Debug Statement*/
        
        //If there are NOT schematic files in the world folder of the world the chunks are being loaded in prints a message
        if(schematicsForWorldGen.isEmpty() && schematicsForBiomeGen.isEmpty()) {
    		File chunkFile = new File("plugins/Easy_Structures/PopulatedChunks/" + "/" + world.getName() + "/" + "chunk_" + chunk.getX() + "," + chunk.getZ() + ".yml");
    		if(!chunkFile.exists()) {
    			createChunkFile("false");
    		}
            return;
        } else {
        	createChunkFile("true");
        }
        
        //Checks the schematics config file to see what the chance is for this schematic to be spawned
        ArrayList<String> chosenWorldSchematics = schematicChance(schematicsForWorldGen, worldPath);
        ArrayList<String> chosenBiomeSchematics = schematicChance(schematicsForBiomeGen, biomePath);
        
		//Instantiating an ArrayList to hold all of the schematics for generation
		ArrayList<String> chosenSchematics = chosenWorldSchematics;
		chosenSchematics.addAll(chosenBiomeSchematics);
        
        while (chunkChance() == true) {
	        
	        String chosenSchematic = null;
	        
	        //Grabs a random schematic from chosenSchemeNames[] and puts it into schemeName
	        //Not sure why they did it this way
	        //Seems like they're misrepresenting the spawn rate. I might want to take this out?
	        if (chosenSchematics.size() > 1) {
		        chosenSchematic = chosenSchematics.get(rand.nextInt(chosenSchematics.size()));
	        } else {
	        	chosenSchematic = chosenSchematics.get(0);
	        }
	        
	        //Grabbing the configuration file for the chosen schematic
	        String chosenConfig = chosenSchematic.substring(0, chosenSchematic.indexOf('.'));
	        
	        BetterConfiguration schematicConfig = WorldFeatures.getConfig(new StringBuilder(worldPath).append("/").append(chosenConfig).toString());
	        
	        //Checks configs for maxSpawns. If the schematic has reached its max spawns it exits this method
	        if(reachedMaxSpawns(schematicConfig) == true) {
	        	return;
	        }
	        
	        if (worldPath.contains(chosenSchematic)) {
		        //Loading the schematic to the CuboidClipboard
		        cc = loadSchematic(worldPath, chosenSchematic);
	        } else {
	        	cc = loadSchematic(biomePath, chosenSchematic);
	        }
	        
	        //Getting the width, length, and height of the schematic that was just loaded into the clipboard
	        width = cc.getWidth();
	        
	        //If width and length are even then setting the origin to 0,0 doesn't allow my corner block calculations to work.
	        //This is a quick fix. If I could figure out which of the 4 center blocks it chooses as it's 0,0 point I could adjust the math
	        if (width % 2 == 0) {
	        	width++;
	        }
	        
	        length = cc.getLength();
	        if (length % 2 == 0) {
	        	length++;
	        }
	        
	        height = cc.getHeight();
	        
	        //Using the offSet Vector setting the schem's offest to 0
	        cc.setOffset(offSet);
	        
	        Vector offsetToMid = new Vector((-width / 2), 0, (-length / 2));
	        
	        cc.setOffset(offsetToMid);
	        
	        //Checks config for randomRotate. If true, gets a random rotation for the schematic and applies it
	        randomRotate(schematicConfig, cc);
	        
	        //Checking config to see where place is set to and positions the schematic in that place
	        boolean canSpawn = placeToSpawn(schematicConfig, maxHeight);
	        
	        //If canSpawn is true, the schematic will paste
	        Block spawnLocation = spawn(canSpawn, schematicConfig);
	        
	        masking(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), schematicConfig.getString("masking", "SPONGE"));
        }
//////////////////////////////End of the world's schematics////////////////////////////////////////////
        
        
        
//////////////////////////////Getting the biome schematics/////////////////////////////////////////////
/*      //Setting the path to look in the biome folder of the biome the block is currently in
        String biomePath = "plugins/Easy_Structures/Schematics/" + "/" + loadBlockInChunk(randX, baseHeight, randZ).getBiome().toString();
        
		//Grabbing all files that are inside the biome folder
        String[] filesInBiomeFolder = new File(biomePath).list();
        
        //Instantiating an arraylist to hold the schematics that are inside the biome folder
		ArrayList<String> schematicsForBiomeGen = new ArrayList<String>();
		
        //Looks at the list of files in the biomePath and grabs all schematic files
        schematicsForBiomeGen = schematicsForGen(filesInBiomeFolder);
        
        //If there are NOT schematic files in the biome folder of the world the chunks are being loaded in prints a message
        if(schematicsForBiomeGen.size() == 0) {
        	if (plugin.getConfig().getBoolean("debug") == true) {
        		WorldFeatures.log.info("Did not find any schematics in folder: " + loadBlockInChunk(randX, baseHeight, randZ).getBiome().toString() + "!");
        	}
        	return;
        }
        
        //Checks the schematics config file to see what the chance is for this schematic to be spawned
        ArrayList<String> chosenBiomeSchematics = schematicChance(schematicsForBiomeGen, biomePath);
        
        //If there are no chosenSchemeNames it returns
        if(chosenBiomeSchematics.isEmpty()) {
            return;
        }
        
        //Grabs a random schematic from chosenSchemeNames[] and puts it into schemeName
        //Not sure why they did it this way
        //Seems like they're misrepresenting the spawn rate. I might want to take this out?
        String chosenBiomeSchematic = chosenBiomeSchematics.get(rand.nextInt(chosenBiomeSchematics.size()));
        
        //Grabbing the configuration file for the chosen schematic
        String chosenBiomeConfig = chosenBiomeSchematic.substring(0, chosenBiomeSchematic.indexOf('.'));
        
        BetterConfiguration biomeSchematicConfig = WorldFeatures.getConfig(new StringBuilder(biomePath).append("/").append(chosenBiomeConfig).toString());
        
        //Checks configs for maxSpawns. If the schematic has reached its max spawns it exits this method
        if(reachedMaxSpawns(biomeSchematicConfig) == true) {
        	return;
        }
        
        //Loading the schematic to the CuboidClipboard
        cc = loadSchematic(biomePath, chosenBiomeSchematic);
        
        //Getting the width, length, and height of the schematic that was just loaded into the clipboard
        width = cc.getWidth();
        if (width % 2 == 0) {
        	width++;
        }
        length = cc.getLength();
        if (length % 2 == 0) {
        	length++;
        }
        height = cc.getHeight();
        
        //Setting the offSet for the Biome schem to 0
        cc.setOffset(offSet);
        
        //Checks config for randomRotate. If true, gets a random rotation for the schematic and applies it
        randomRotate(biomeSchematicConfig, cc);
        
        //Checking config to see where place is set to and positions the schematic in that place
        canSpawn = placeToSpawn(biomeSchematicConfig, maxHeight);
        
        //If canSpawn is true, the schematic will paste
        spawn(canSpawn, biomeSchematicConfig);
//////////////////////////////End of the biome schematics/////////////////////////////////////////// */
    }
    
    /**
     * Allowing WorldFeatures class to be aware of this particular instance of chunkListener.
     * @param main An instance of the WorldFeatures class
     */
    public ChunkListener(WorldFeatures main) { 
    	ChunkListener.plugin = main;
    }
    
    /**
     * Just another constructor.
     * Adds defaults to rand, width, height, and length.
     */
    public ChunkListener() {
        rand = new Random();
        width = 0;
        length = 0;
        height = 0;
    }
    
    /**
     * Spawns the schematic in the world.
     * @param world The world to spawn the schematic in.
     * @param origin The block where the schematic will be pasted.
     * @param pasteNoneOfThese An integer array, not sure what it is really....
     */
    private void loadArea(World world, Vector origin, int[] pasteNoneOfThese) {
        EditSession es = new EditSession(new BukkitWorld(world), 1000000);
        try  {
            cc.paste(es, origin, true);
        } catch(MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Loads a block within the chunk. Automagically takes care of any maths.
     * The X and Z value should be 0 - 16 since you're only inside the chunk at this point.
     * @param x the X coord for the block
     * @param y the height of the block
     * @param z the Z coord for the block
     * @return The block at the specified coordinates
     */
    public Block loadBlockInChunk(int x, int y, int z) {
        return world.getBlockAt(chunkX + x, y, chunkZ + z);
    }
    
    /**
     * Checks if one or more corner blocks of the bottom most section of the cuboid clipboard is of a certain material.
     * @param material The material you want to check for (Basically the different types of blocks in MC).
     * @return True if one or more of the blocks are the specified material, false otherwise.
     * @author Evan Tellep
     */
    public boolean bottomCornerBlocksOr(Material material) {
        return loadBlockInChunk(randX - (width / 2), baseHeight, randZ - (length / 2)).getType() == material 
        	|| loadBlockInChunk(randX - (width / 2), baseHeight, randZ + (length / 2)).getType() == material 
        	|| loadBlockInChunk(randX + (width / 2), baseHeight, randZ - (length / 2)).getType() == material 
        	|| loadBlockInChunk(randX + (width / 2), baseHeight, randZ + (length / 2)).getType() == material;
    }
    
    /**
     * Checks if one or more corner blocks of the top most section of the cuboid clipboard is of a certain material.
     * @param material The material you want to check for (Basically the different types of blocks in MC).
     * @return True if one or more of the blocks are the specified material, false otherwise.
     * @author Evan Tellep
     */
    public boolean topCornerBlocksOr(Material material) {
        return loadBlockInChunk(randX - (width / 2), baseHeight + height - 1, randZ - (length / 2)).getType() == material 
        	|| loadBlockInChunk(randX - (width / 2), baseHeight + height - 1, randZ + (length / 2)).getType() == material 
        	|| loadBlockInChunk(randX + (width / 2), baseHeight + height - 1, randZ - (length / 2)).getType() == material 
        	|| loadBlockInChunk(randX + (width / 2), baseHeight + height - 1, randZ + (length / 2)).getType() == material;
    }
    
    /**
     * Checks to see if all corner blocks of the bottom most section of the cuboid clipboard is of a specific material.
     * @param material The material you want to check for.
     * @return True if all of the corner blocks are the specified material, false otherwise.
     * @author Evan Tellep
     */
    public boolean bottomCornerBlocksAnd(Material material) {
        return loadBlockInChunk(randX - (width / 2), baseHeight, randZ - (length / 2)).getType() == material 
        	&& loadBlockInChunk(randX - (width / 2), baseHeight, randZ + (length / 2)).getType() == material 
        	&& loadBlockInChunk(randX + (width / 2), baseHeight, randZ - (length / 2)).getType() == material 
        	&& loadBlockInChunk(randX + (width / 2), baseHeight, randZ + (length / 2)).getType() == material;
    }
    
    /**
     * Checks if all corner blocks of the top most section of the cuboid clipboard is of a certain material.
     * @param material The material you want to check for.
     * @return True if all of the corner blocks are the specified material, false otherwise.
     * @author Evan Tellep
     */
    public boolean topCornerBlocksAnd(Material material) {
        return loadBlockInChunk(randX - (width / 2), baseHeight + height - 1, randZ - (length / 2)).getType() == material 
        	&& loadBlockInChunk(randX - (width / 2), baseHeight + height - 1, randZ + (length / 2)).getType() == material 
        	&& loadBlockInChunk(randX + (width / 2), baseHeight + height - 1, randZ - (length / 2)).getType() == material 
        	&& loadBlockInChunk(randX + (width / 2), baseHeight + height - 1, randZ + (length / 2)).getType() == material;
    }
    
    /**
     * Checks to see if all the corner blocks are of a specific biome.
     * @param biome The biome you want to check for.
     * @return True if all the corner blocks are of the specified biome, false otherwise.
     */
    public boolean cornerBlocksBiomeAnd(Biome biome) {
        return loadBlockInChunk(randX, baseHeight, randZ).getBiome().toString().equals(biome) 
        	&& loadBlockInChunk(randX + width, baseHeight, randZ).getBiome().toString().equals(biome) 
        	&& loadBlockInChunk(randX, baseHeight, randZ + length).getBiome().toString().equals(biome) 
        	&& loadBlockInChunk(randX + width, baseHeight, randZ + length).getBiome().toString().equals(biome);
    }
    
    /**
     * Checks to see if one or more of the corner blocks is of a specific biome.
     * @param biome The biome you want to check for.
     * @return True if one or more blocks is of the specified biome, false otherwise
     * @author Evan Tellep
     */
    public boolean cornerBlocksBiomeOr(Biome biome) {
        return loadBlockInChunk(randX, baseHeight, randZ).getBiome().toString().equals(biome) 
        	|| loadBlockInChunk(randX + width, baseHeight, randZ).getBiome().toString().equals(biome) 
        	|| loadBlockInChunk(randX, baseHeight, randZ + length).getBiome().toString().equals(biome) 
        	|| loadBlockInChunk(randX + width, baseHeight, randZ + length).getBiome().toString().equals(biome);
    }
    
    /**
     * Checks to see if the chunk has a chance to spawn anything
     * @return True if chunkChance from the plugin config is greater than a random number between 0-100, false otherwise.
     */
    public boolean chunkChance() {
    	int chunkChance = plugin.getConfig().getInt("chunkchance", 100);
    	while (chunkChance > 0) {
	        if(rand.nextInt(100) + 1 > chunkChance) {
	        	if (plugin.getConfig().getBoolean("debug") == true)
	        	{
	        		WorldFeatures.log.info("Chunk Chance: Not going to load schematics in newly created chunk");
	        	}
	        	chunkChance = chunkChance - 100;
	            return false;
	        }
	        chunkChance = chunkChance - 100;
			return true;
    	}
    	return true;
    }
    
    /**
     * Creates the folders for the world that this event is being triggered in, but only if they have not yet been made.
     * @param path The path to be created
     * @author Evan Tellep
     */
    public void genFolders(String path) {
	    if (!new File(path).exists()) {
	    	new File(path).mkdirs();
	    	Biome[] biomes = Biome.values();
	    	for (int i = 0; i < biomes.length; i++) {
	    		File biomeFldrs = new File("plugins/Easy_Structures/Schematics/" + "/" + world.getName() + "/" + biomes[i]);
	    		biomeFldrs.mkdirs();
	    	}
	    }
    }
    
	/**
	 * Looks through a list of files from a directory, if any are of the schematic file type it adds those to this list.
	 * @param filesList The file list to be checked for schematic files.
	 * @return The list of all schematic files within the specified file list
	 */
    public ArrayList<String> schematicsForGen(String[] filesList) {
		ArrayList<String> schematicsForWorldGen = new ArrayList<String>();
	    if(filesList != null) {
	    	if (plugin.getConfig().getBoolean("debug") == true)
	    	{
	    		WorldFeatures.log.info("Found schematics in folder: " + world.getName());
	    	}
	        for(int i = 0; i < filesList.length; i++) {
	        	String fileType = filesList[i].substring(filesList[i].indexOf('.') + 1);
	            if(fileType.equals("schematic")) {
	            	schematicsForWorldGen.add(filesList[i]);
	            }
	        }
	    }
		return schematicsForWorldGen;
    }
    
    /**
     * Checks the schematics config for it's chance to spawn. If the schematics chance is greater than a random int from 0-100
     * it adds the schematic to an arraylist.
     * @param schematicsInFolder List of all schematics in a folder.
     * @param pathToCheckForConfig The path to look in for the schematic's config.
     * @return The list of chosen schematics that passed the check.
     */
    public ArrayList<String> schematicChance(ArrayList<String> schematicsInFolder, String pathToCheckForConfig) {
        ArrayList<String> chosenSchemeNames = new ArrayList<String>();
        for(int i = 0; i < schematicsInFolder.size(); i++) {
        	String name = schematicsInFolder.get(i);
            BetterConfiguration config = WorldFeatures.getConfig(new StringBuilder(pathToCheckForConfig).append("/").append(name.substring(0, name.indexOf("."))).toString());
            int chance = config.getInt("chance", 50);
            while (chance > 0) {
	            if(rand.nextInt(100) + 1 <= chance) {
	            	chosenSchemeNames.add(name);
	            }
	            chance = chance - 100;
        	}
        }
		return chosenSchemeNames;
    }
    
    /**
     * Loads the schematic into a CuboidClipboard.
     * @param path Path where the schematic is located.
     * @param schematicName The name of the schematic to be loaded.
     * @return The Cuboid Clipboard that has the loaded schematic.
     */
    public CuboidClipboard loadSchematic(String path, String schematicName) {
	    try {
	        File file = new File(new StringBuilder(path).append("/").append(schematicName).toString());
	        cc = MCEditSchematicFormat.MCEDIT.load(file);
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
		return cc;
    }
    
    
    //Seems to set a max num of times the schematic can spawn? If number of times spawn is greater than the set value
    //from config, returns
    //Don't think this will work unless we put a static variable to keep track of how many times the specific schem has been spawned
    //Might be easier to just write a counter to the schematic's config file
    public boolean reachedMaxSpawns(BetterConfiguration configFile) {
	    int maxSpawns = configFile.getInt("maxspawns", 0);
	    if(maxSpawns != 0 && configFile.getInt((new StringBuilder("spawns.")).append(world.getName()).toString(), 0) >= maxSpawns) {
	        return true;
	    }
	    return false;
    }
    
    /**
     * Checks the schematics Configuration file for the value of randomRotate, if it is true, rotates the schematic
     * @param config The schematics configuration file.
     * @param cc The Cuboid Clipboard to be rotated
     */
    public void randomRotate(BetterConfiguration config, CuboidClipboard cc) {
	    if(config.getBoolean("randomrotate", true)) {
	        rotation = rand.nextInt(4) * 90;
	        cc.rotate2D(rotation);
	        switch(rotation) {
	            case 90:
	                width = - cc.getWidth();
	                length = cc.getLength();
	                break;
	
	            case 180: 
	                width = - cc.getWidth();
	                length = - cc.getLength();
	                break;
	
	            case 270: 
	                width = cc.getWidth();
	                length = - cc.getLength();
	                break;
	        }
	    }
    }
    
    /**
     * Checks the configuration file to see what place is set to, and works to make that true.
     * @param config The schematics configuration file.
     * @param maxHeight The max height of the world.
     * @return True if the object meets the specified conditions for it's 'place' and it can spawn, false otherwise.
     */
    public boolean placeToSpawn(BetterConfiguration config, int maxHeight) {
    	boolean canSpawn = true;
	    String place = config.getString("place", "ground");
	    
	    switch (place) {
	    
		    case "anywhere":
		    	int minY = config.getInt("anywhereminY", 1);
		    	int maxY = config.getInt("anywheremaxY", maxHeight);
		    	baseHeight = rand.nextInt(maxY - minY) + 1 + minY;
		    	if(baseHeight + height - 1 > maxHeight) {
		    		canSpawn = false;
		    	}
		    	
		    case "ground":
		        baseHeight = maxHeight;
		        int basement = config.getInt("basementdepth", 0);
		        while(bottomCornerBlocksOr(Material.AIR) || bottomCornerBlocksOr(Material.LEAVES) || bottomCornerBlocksOr(Material.LEAVES_2)
		        		|| bottomCornerBlocksOr(Material.LOG) || bottomCornerBlocksOr(Material.LOG_2) || bottomCornerBlocksOr(Material.SNOW) 
		        		|| bottomCornerBlocksOr(Material.LONG_GRASS) || bottomCornerBlocksOr(Material.PACKED_ICE))  {
		            baseHeight--;
		        }
		        if(bottomCornerBlocksOr(Material.STATIONARY_WATER) || bottomCornerBlocksOr(Material.ICE) || bottomCornerBlocksOr(Material.PACKED_ICE) 
		        		|| baseHeight + height > maxHeight) {
		            canSpawn = false;
		            if(loadBlockInChunk(randX, baseHeight, randZ).getBiome() == Biome.SWAMPLAND || loadBlockInChunk(randX, baseHeight, randZ).getBiome() == Biome.MUTATED_SWAMPLAND) {
		            	while (bottomCornerBlocksOr(Material.STATIONARY_WATER)) {
		            		baseHeight--;
		            	}
		            	canSpawn = true;
		            }
		        }
		        for(int i = 0; i < basement; i++) {
		        	baseHeight--;
		        }
		        
		    case "air":
		        baseHeight = maxHeight;
		        while (loadBlockInChunk(randX, baseHeight, randZ).getType() == Material.AIR) {
		        	baseHeight--;
		        }
		        baseHeight = rand.nextInt(maxHeight - baseHeight) + baseHeight;
		        if(!bottomCornerBlocksAnd(Material.AIR) || !topCornerBlocksAnd(Material.AIR) || baseHeight + height - 1 > maxHeight) {
		            canSpawn = false;
		        }
		        
			case "underground":
		    	while(loadBlockInChunk(randX, baseHeight + height, randZ).getType() != Material.AIR) {
		    		baseHeight++;
		    	}
		        baseHeight--;
		        baseHeight = rand.nextInt(baseHeight);
		        if(topCornerBlocksOr(Material.AIR)) {
		        	while(topCornerBlocksOr(Material.AIR)) {
		        		baseHeight--;
		        	}
		        	baseHeight--;
		        }
		        if(baseHeight + height - 1 > maxHeight) {
		            canSpawn = false;
		        }
	    }
	    /*
	    if(place.equals("anywhere")) {
	        int minY = config.getInt("anywhereminY", 1);
	        int maxY = config.getInt("anywheremaxY", maxHeight);
	        baseHeight = rand.nextInt(maxY - minY) + 1 + minY;
	        if(baseHeight + height - 1 > maxHeight) {
	            canSpawn = false;
	        }
	        
	    } else if(place.equals("ground")) {
	        baseHeight = maxHeight;
	        int basement = config.getInt("basementdepth", 0);
	        while(bottomCornerBlocksOr(Material.AIR) || bottomCornerBlocksOr(Material.LEAVES) || bottomCornerBlocksOr(Material.LEAVES_2)
	        		|| bottomCornerBlocksOr(Material.LOG) || bottomCornerBlocksOr(Material.LOG_2) || bottomCornerBlocksOr(Material.SNOW) 
	        		|| bottomCornerBlocksOr(Material.LONG_GRASS) || bottomCornerBlocksOr(Material.PACKED_ICE))  {
	            baseHeight--;
	        }
	        if(bottomCornerBlocksOr(Material.STATIONARY_WATER) || bottomCornerBlocksOr(Material.ICE) || bottomCornerBlocksOr(Material.PACKED_ICE) 
	        		|| baseHeight + height > maxHeight) {
	            canSpawn = false;
	        }
	        for(int i = 0; i < basement; i++) {
	        	baseHeight--;
	        }
	        
	    } else if(place.equals("air")) {
	        baseHeight = maxHeight;
	        while (loadBlockInChunk(randX, baseHeight, randZ).getType() == Material.AIR) {
	        	baseHeight--;
	        }
	        baseHeight = rand.nextInt(maxHeight - baseHeight) + baseHeight;
	        if(!bottomCornerBlocksAnd(Material.AIR) || !topCornerBlocksAnd(Material.AIR) || baseHeight + height - 1 > maxHeight) {
	            canSpawn = false;
	        }
	        
	    } else if(place.equals("underground")) {
	    	while(loadBlockInChunk(randX, baseHeight + height, randZ).getType() != Material.AIR) {
	    		baseHeight++;
	    	}
	        baseHeight--;
	        baseHeight = rand.nextInt(baseHeight);
	        if(topCornerBlocksOr(Material.AIR)) {
	        	while(topCornerBlocksOr(Material.AIR)) {
	        		baseHeight--;
	        	}
	        	baseHeight--;
	        }
	        if(baseHeight + height - 1 > maxHeight) {
	            canSpawn = false;
	        }
	    }*/
		return canSpawn;
    }
    
    /**
     * Create a file for the chunk to determine whether this plugin has or has not tried to populate the chunk
     * @param value True/False Whether or not the plugin has tried to populate the chunk
     */
    public void createChunkFile(String value) {
		//creates a null buffered writer object
		BufferedWriter writer = null;
		//tries to create and write a new YML file
		try
		{
			//adds the ".yml" extension to the file name
			String fileName = "chunk_" + chunk.getX() + "," + chunk.getZ() + ".yml";
    		File chunkFileLocation = new File("plugins/Easy_Structures/PopulatedChunks/" + "/" + world.getName());
    		chunkFileLocation.mkdirs();
			//Creates a new file object with the file name
			File file = new File("plugins/Easy_Structures/PopulatedChunks/" + "/" + world.getName() + "/" + fileName);
			//creates a blank file from the file object with the given file name
			file.createNewFile();
			//finds the exact path of the created file object to ensure writing to it is successful 
			String filePath = file.getCanonicalPath();
			//Instantiate the buffered writer object wrapped around a file writer object
			writer = new BufferedWriter(new FileWriter(filePath));
			//Writes individual attributes to the file with a line break at the end of each attribute IF they actually entered said attributes
			writer.write("populated: " + value);
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
    
    /**
     * Spawns the schematic in the world
     * @param canSpawn Whether or not the schematic can spawn.
     * @param schematicConfig The configuration file of the schematic to be spawned.
     */
    public Block spawn(boolean canSpawn, BetterConfiguration schematicConfig) {
	    if(canSpawn) {
	    	if (plugin.getConfig().getBoolean("showspawnedlocation") == true)
	    	{
	    		WorldFeatures.log.info("Spawning schematic at chunk (x,z)" + chunk.getX() + "," + chunk.getZ());
	    	}
	    	String[] stringNone = schematicConfig.getString("dontpaste", "0").replaceAll(" ", "").split(",");
	    	int[] pasteNone = new int[stringNone.length];
	    	int i = 0;
	    	for(String s : stringNone) {
	    		pasteNone[i] = Integer.parseInt(s);
	    		i++;
	    	}
	        loadArea(world, new Vector(chunkX + randX, (baseHeight + 1) - schematicConfig.getInt("basementdepth", 0), chunkZ + randZ), pasteNone);
	        schematicConfig.set((new StringBuilder("spawns.")).append(world.getName()).toString(), schematicConfig.getInt((new StringBuilder("spawns.")).append(world.getName()).toString(), 0) + 1);
	    }
	    return loadBlockInChunk((randX),((baseHeight + 1) - schematicConfig.getInt("basementdepth", 0)),(randZ));
    }
    
    /**
     * Masking allows you to fill your schematic with a non-natural block in place of air, that way
     * after the schematic spawns nothing appears inside of it. Helpful if you are spawning houses
     * and are afraid trees might show up in it or it might spawn partially inside of a hill.
     * @param x The x coordinate of the origin block.
     * @param y The y coordinate of the origin block.
     * @param z The z coordinate of the origin block.
     * @param maskingValue The material you are using for masking
     * @author Evan Tellep
     */
    public void masking(int x, int y, int z, String maskingValue) {
    	Material maskingMaterialValue = Material.getMaterial(maskingValue);
    	/*
    	 * Due to randomRotate sometimes the length/width values are negative
    	 * so this logic is to make sure they are always positive.
    	 */
    	if (length < 0) {
    		length = length * -1;
    	}
    	if (width < 0) {
    		width = width * -1;
    	}
    	/*
    	 * Since the origin is in the middle of the schematic I have to use the origin as a reference and move
    	 * to the corner block to allow me to easily cycle through the entire schematic.
    	 */
    	x = x + (length / 2) + 1;
    	z = z + (width / 2) + 1;
    	/*
    	 * This is the series of for loops used to cycle through the schematic.
    	 */
    	for (int i = 0; i < length; i++){
    			x--;
    		for (int j = 0; j < width; j++) {
        			z--;
    			for (int k = 0; k < height; k++) {
    				/*
    				 * Loads a block of the schematic and checks to see if it matches the selected masking
    				 * material. If it does it replaces the masking material with air.
    				 */
    		    	Block temp = loadBlockInChunk(x - chunkX, y, z - chunkZ);
    		    	if (temp.getType() == maskingMaterialValue) {
    		    		temp.setType(Material.AIR);
    		    	}
        		    	y++;
    			}
    			//Resetting the Y value back to the bottom of the schematic.
    			y = y - height;
    		}
    		//Resetting the Z value back to the edge of the schematic.
    		z = z + width;
    	}
    }
    
    /**
     * Turns the default leaves and wood into air
     * @author Jake Reilman
     * @param y coordinate
     * @deprecated Doesn't work due to events occurring during the onChunkPopulate event not being synced. Sometimes the plugin runs second, running this method and removing trees,
     * sometimes the default MC populators run second, causing the trees to spawn in after this method runs.
     */
    public void noDefaultTrees(int x, int y, int z) {
	    for (int i = 0; i < 16; i++){
			x++;
			for (int j = 0; j < 16; j++) {
	    		z++;
				for (int k = 0; k < world.getMaxHeight() - 1; k++) {
					/*
					 * Loads a block of the schematic and checks to see if it matches the selected masking
					 * material. If it does it replaces the masking material with air.
					 */
			    	Block temp = loadBlockInChunk(x - chunkX, y, z - chunkZ);
			    	if (temp.getType() == Material.LEAVES || temp.getType() == Material.LEAVES_2 || temp.getType() == Material.LOG || temp.getType() == Material.LOG_2) {
			    		temp.setType(Material.AIR);
			    	}
	    		    	y--;
				}
				//Resetting the Y value back to the bottom of the schematic.
				y = y + world.getMaxHeight() - 1;
			}
			//Resetting the Z value back to the edge of the schematic.
			z = z - 16;
		}
    }
}


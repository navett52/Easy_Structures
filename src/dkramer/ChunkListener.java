package dkramer;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

public class ChunkListener implements Listener {
	//A random number generator
	Random rand;
	//The chunk being populated
    private Chunk chunk;
    // base height? basement height? not sure yet..
    private int bHeight;
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
    public static WorldFeatures plugin;
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
        //Setting the default that each schematic can spawn unless proven otherwise
        boolean canSpawn = true;
        //Getting the maxHeight of the world
        int maxHeight = world.getMaxHeight() - 1;
        //Looks in the plugins config file at chunkChance to determine if this chunk will even spawn anything
        if(chunkChance() == false) {
        	return;
        }
//////////////////////////////Getting the world schematics///////////////////////////////////////////   
        //Creating the file path to be looked into for schematic files to be generated in the world
        String worldPath = "plugins/Easy_Structures/Schematics/" + "/" + world.getName();
        //Creating arraylist to hold the schematic files from the world folder
		ArrayList<String> schematicsForWorldGen = new ArrayList<String>();
		//Getting all files within the worldPath
		String[] filesInWorldFolder = new File(worldPath).list();
        //Automagically generating the file paths for the world if it is new.
		//Unfortunately it does not automagically populate the folders with schematics
        genFolders(worldPath);  
        //Looks at the list of files in the worldPath and grabs all schematic files
        schematicsForWorldGen = schematicsForGen(filesInWorldFolder);
        //If there are NOT schematic files in the world folder of the world the chunks are being loaded in prints a message
        if(schematicsForWorldGen.size() == 0) {
        	WorldFeatures.log.info("Did not find any schematics in folder: " + world.getName() + "!");
            return;
        }
        //Checks the schematics config file to see what the chance is for this schematic to be spawned
        ArrayList<String> chosenWorldSchematics = schematicChance(schematicsForWorldGen, worldPath);
        //If there are no chosenSchemeNames it returns
        if(chosenWorldSchematics.isEmpty()) {
            return;
        }
        //Grabs a random schematic from chosenSchemeNames[] and puts it into schemeName
        //Not sure why they did it this way
        //Seems like they're misrepresenting the spawn rate. I might want to take this out?
        String chosenWorldSchematic = chosenWorldSchematics.get(rand.nextInt(chosenWorldSchematics.size()));
        //Grabbing the configuration file for the chosen schematic
        String chosenWorldConfig = chosenWorldSchematic.substring(0, chosenWorldSchematic.indexOf('.'));
        BetterConfiguration worldSchematicConfig = WorldFeatures.getConfig(new StringBuilder(worldPath).append("/").append(chosenWorldConfig).toString());
        //Checks configs for maxSpawns. If the schematic has reached its max spawns it exits this method
        if(reachedMaxSpawns(worldSchematicConfig) == true) {
        	return;
        }
        //Loading the schematic to the CuboidClipboard
        cc = loadSchematic(worldPath, chosenWorldSchematic);
        //Getting the width, length, and height of the schematic that was just loaded into the clipboard
        width = cc.getWidth();
        length = cc.getLength();
        height = cc.getHeight();
        //Checks config for randomRotate. If true, gets a random rotation for the schematic and applies it
        randomRotate(worldSchematicConfig, cc);
        //Checking config to see where place is set to and positions the schematic in that place
        canSpawn = placeToSpawn(worldSchematicConfig, maxHeight);
        //If canSpawn is true, the schematic will paste
        spawn(canSpawn, worldSchematicConfig);
//////////////////////////////Getting the world schematics///////////////////////////////////////////
//////////////////////////////Getting the biome schematics///////////////////////////////////////////
        //Setting the path to look in the biome folder of the biome the block is currently in
        String biomePath = "plugins/Easy_Structures/Schematics/" + "/" + loadBlockInChunk(randX, bHeight, randZ).getBiome().toString();
		//Grabbing all files that are inside the biome folder
        String[] filesInBiomeFolder = new File(biomePath).list();
        //Instantiating an arraylist to hold the schematics that are inside the biome folder
		ArrayList<String> schematicsForBiomeGen = new ArrayList<String>();
        //Looks at the list of files in the biomePath and grabs all schematic files
        schematicsForBiomeGen = schematicsForGen(filesInBiomeFolder);
        //If there are NOT schematic files in the biome folder of the world the chunks are being loaded in prints a message
        if(schematicsForBiomeGen.size() == 0) {
        	WorldFeatures.log.info("Did not find any schematics in folder: " + loadBlockInChunk(randX, bHeight, randZ).getBiome().toString() + "!");
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
        length = cc.getLength();
        height = cc.getHeight();
        //Checks config for randomRotate. If true, gets a random rotation for the schematic and applies it
        randomRotate(biomeSchematicConfig, cc);
        //Checking config to see where place is set to and positions the schematic in that place
        canSpawn = placeToSpawn(biomeSchematicConfig, maxHeight);
        //If canSpawn is true, the schematic will paste
        spawn(canSpawn, biomeSchematicConfig);
//////////////////////////////Getting the biome schematics///////////////////////////////////////////
    }
    
    /**
     * Allowing WorldFeatures calss to be aware of this particular instance of chunkListener.
     * @param main
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
     * Checks if one or more corner blocks is of a certain material.
     * @param material The material you want to check for (Basically the different types of blocks in MC).
     * @return True if one or more of the blocks are the specified material, false otherwise.
     */
    public boolean cornerBlocksOr(Material material) {
        return loadBlockInChunk(randX, bHeight, randZ).getType() == material 
        	|| loadBlockInChunk(randX + width, bHeight, randZ).getType() == material 
        	|| loadBlockInChunk(randX, bHeight, randZ + length).getType() == material 
        	|| loadBlockInChunk(randX + width, bHeight, randZ + length).getType() == material;
    }
    
    /*//Nave
    //Should allow us to check if at least one of the corner blocks is in a biome
    //Should allow for nice transition structures
    public Biome[] cornerBlocksBiomes() {
    	Biome[] biomes = new Biome[4];
        biomes[0] = loadBlockChunk(randX, bHeight, randZ).getBiome();
        biomes[1] = loadBlockChunk(randX + width, bHeight, randZ).getBiome(); 
        biomes[2] = loadBlockChunk(randX, bHeight, randZ + length).getBiome(); 
        biomes[3] = loadBlockChunk(randX + width, bHeight, randZ + length).getBiome();
		return biomes;
    }*/ //Might not need this method
    
    /**
     * Checks to see if all corner blocks are a specific material.
     * @param material The material you want to check for.
     * @return True if all the corner blocks are the specified material, false otherwise.
     */
    public boolean cornerBlocksAnd(Material material) {
        return loadBlockInChunk(randX, bHeight, randZ).getType() == material
        	&& loadBlockInChunk(randX + width, bHeight, randZ).getType() == material
        	&& loadBlockInChunk(randX, bHeight, randZ + length).getType() == material 
        	&& loadBlockInChunk(randX + width, bHeight, randZ + length).getType() == material;
    }
    
    /**
     * Checks to see if all the corner blocks are of a specific biome.
     * @param biome The biome you want to check for.
     * @return True if all the corner blocks are of the specified biome, false otherwise.
     */
    public boolean cornerBlocksBiomeAnd(Biome biome) {
        return loadBlockInChunk(randX, bHeight, randZ).getBiome().toString().equals(biome) 
        	&& loadBlockInChunk(randX + width, bHeight, randZ).getBiome().toString().equals(biome) 
        	&& loadBlockInChunk(randX, bHeight, randZ + length).getBiome().toString().equals(biome) 
        	&& loadBlockInChunk(randX + width, bHeight, randZ + length).getBiome().toString().equals(biome);
    }
    
    /**
     * Checks to see if one or more of the corner blocks is of a specific biome.
     * @param biome The biome you want to check for.
     * @return True if one or more blocks is of the specified biome, false otherwise
     */
    public boolean cornerBlocksBiomeOr(Biome biome) {
        return loadBlockInChunk(randX, bHeight, randZ).getBiome().toString().equals(biome) 
        	|| loadBlockInChunk(randX + width, bHeight, randZ).getBiome().toString().equals(biome) 
        	|| loadBlockInChunk(randX, bHeight, randZ + length).getBiome().toString().equals(biome) 
        	|| loadBlockInChunk(randX + width, bHeight, randZ + length).getBiome().toString().equals(biome);
    }
    
    /**
     * Checks to see if the chunk has a chance to spawn anything
     * @return True if chunkChance from the plugin config is greater than a random number between 0-100, false otherwise.
     */
    public boolean chunkChance() {
        if(rand.nextInt(100) + 1 > plugin.getConfig().getInt("chunkchance")) {
        	if (plugin.getConfig().getBoolean("debug") == true)
        	{
        		WorldFeatures.log.info("Not going to load schematics in newly created chunk");
        	}
            return false;
        }
		return true;
    }
    
    /**
     * Checks to see the biome of a specified block within the chunk
     * @param x the X coord of the block within the chunk
     * @param y the height of the block
     * @param z the Z coord of the block within the chunk
     * @return The name of the biome the block is in
     */
    public String biomeOfBlock(int x, int y, int z) {
		String biomeOfRandBlock = loadBlockInChunk(x, y, z).getBiome().toString();
		return biomeOfRandBlock;
    }
    
    /**
     * Creates the folders for the world that this event is being triggered in if they have not yet been made.
     * @param path The path to be created
     */
    public void genFolders(String path) {
	    File worldPath = new File(path);
	    if (!worldPath.exists()) {
	    	worldPath.mkdirs();
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
            if(rand.nextInt(100) + 1 <= config.getInt("chance", 50)) {
            	chosenSchemeNames.add(name);
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
	    if(place.equals("anywhere")) {
	        int minY = config.getInt("anywhereminY", 1);
	        int maxY = config.getInt("anywheremaxY", maxHeight);
	        bHeight = rand.nextInt(maxY - minY) + 1 + minY;
	        if(bHeight > maxHeight - height) {
	            canSpawn = false;
	        }
	    } else if(place.equals("ground")) { //need to edit this. Since it checks for leaves as well sometimes pushes a schematic too low
	        bHeight = maxHeight;
	        int base = config.getInt("basementdepth", 0);
	        while(cornerBlocksOr(Material.AIR))  {
	            bHeight--;
	        }
	        while(cornerBlocksOr(Material.LEAVES))  {
	            bHeight--;
	        }
	        while(cornerBlocksOr(Material.SNOW))  {
	            bHeight--;
	        }
	        if(bHeight > (maxHeight - height) + base) {
	            canSpawn = false;
	        }
	        if(cornerBlocksOr(Material.STATIONARY_WATER)) {
	            canSpawn = false;
	        }
	    } else if(place.equals("air")) {
	        for(bHeight = maxHeight; cornerBlocksOr(Material.AIR); bHeight--) { }
	        for(bHeight = rand.nextInt(maxHeight - bHeight) + 1 + bHeight; bHeight > maxHeight - height; bHeight--) { }
	        canSpawn = false;
	        if(cornerBlocksAnd(Material.AIR)) {
	            canSpawn = true;
	        }
	    } else if(place.equals("underground")) {
	        for(bHeight = 1; loadBlockInChunk(randX, bHeight + 1 + height, randZ).getType() != Material.AIR; bHeight++) { }
	        for(; loadBlockInChunk(randX + width, bHeight + 1 + height, randZ).getType() != Material.AIR; bHeight++) { }
	        for(; loadBlockInChunk(randX, bHeight + 1 + height, randZ + length).getType() != Material.AIR; bHeight++) { }
	        for(; loadBlockInChunk(randX + width, bHeight + 1 + height, randZ + length).getType() != Material.AIR; bHeight++) { }
	        for(bHeight = rand.nextInt(bHeight) + 1; bHeight > maxHeight - height; bHeight--) { }
	        canSpawn = false;
	        if(loadBlockInChunk(randX, bHeight + 1 + height, randZ).getType() != Material.AIR 
	        		&& loadBlockInChunk(randX + width, bHeight + 1 + height, randZ).getType() != Material.AIR  
	        		&& loadBlockInChunk(randX, bHeight + 1 + height, randZ + length).getType() != Material.AIR  
	        		&& loadBlockInChunk(randX + width, bHeight + 1 + height, randZ + length).getType() != Material.AIR ) {
	            canSpawn = true;
	        }
	    }
		return canSpawn;
    }
    
    /**
     * Spawns the schematic in the world
     * @param canSpawn Whether or not the schematic can spawn.
     * @param schematicConfig The configuration file of the schematic to be spawned.
     */
    public void spawn(boolean canSpawn, BetterConfiguration schematicConfig) {
	    if(canSpawn) {
	    	if (plugin.getConfig().getBoolean("showspawnedlocation") == true)
	    	{
	    		WorldFeatures.log.info("Spawning schematic at chunk (x,z)" + chunkX + "," + chunkZ );	
	    	}
	    	String[] stringNone = schematicConfig.getString("dontpaste", "0").replaceAll(" ", "").split(",");
	    	int[] pasteNone = new int[stringNone.length];
	    	int i = 0;
	    	for(String s : stringNone) {
	    		pasteNone[i] = Integer.parseInt(s);
	    		i++;
	    	}
	        loadArea(world, new Vector(chunkX + randX, (bHeight + 1) - schematicConfig.getInt("basementdepth", 0), chunkZ + randZ), pasteNone);
	        schematicConfig.set((new StringBuilder("spawns.")).append(world.getName()).toString(), schematicConfig.getInt((new StringBuilder("spawns.")).append(world.getName()).toString(), 0) + 1);
	        return;
	    }
    }
    

}
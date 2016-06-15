package dkramer;

import org.bukkit.event.Listener;

public class PlayerListener implements Listener {
	
    public static WorldFeatures plugin;
    
    public PlayerListener(WorldFeatures main) {
    	ChunkListener.plugin = main;
    }
	
    public PlayerListener() {
    }
    
}
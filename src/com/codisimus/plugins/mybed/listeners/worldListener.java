package com.codisimus.plugins.mybed.listeners;

import com.codisimus.plugins.mybed.SaveSystem;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * Loads MyBed data for each World that is loaded
 *
 * @author Codisimus
 */
public class worldListener extends WorldListener{

    /**
     * Loads MyBed data for the loaded World
     * 
     * @param event The WorldLoadEvent that occurred
     */
    @Override
    public void onWorldLoad (WorldLoadEvent event) {
        SaveSystem.load(event.getWorld());
    }
}

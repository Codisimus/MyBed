package com.codisimus.plugins.mybed;

import org.bukkit.block.Block;
import org.bukkit.material.Bed;

/**
 * An OwnedBed is a bed which only the owner can sleep in
 * Others can sleep in it if an Inn sign is present
 *
 * @author Codisimus
 */
public class OwnedBed {
    public String owner;
    public Block head;
    public Block foot;

    /**
     * Constructs a new OwnedBed
     * Gets both blocks of the Bed
     * 
     * @param owner The name of the owner of the OwnedBed
     * @param safe The Block of the OwnedBed
     */
    public OwnedBed(String owner, Block block) {
        this.owner = owner;
        
        Bed bed = (Bed)block.getState().getData();
        
        //Set the Blocks of the Bed
        if (bed.isHeadOfBed()) {
            head = block;
            foot = block.getRelative(bed.getFacing().getOppositeFace());
        }
        else {
            foot = block;
            head = block.getRelative(bed.getFacing());
        }
    }
}

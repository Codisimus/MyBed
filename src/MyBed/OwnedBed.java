
package MyBed;

import org.bukkit.block.Block;
import org.bukkit.material.Bed;

/**
 * An OwnedBed is a bed which only the owner can sleep in
 * Others can sleep in it if an Inn sign is present
 *
 * @author Cody
 */
class OwnedBed {
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
    OwnedBed(String owner, Block block) {
        this.owner = owner;
        Bed bed = (Bed)block.getState().getData();
        if (bed.isHeadOfBed()) {
            head = block;
            foot = block.getRelative(bed.getFacing());
        }
        else {
            foot = block;
            head = block.getRelative(bed.getFacing());
        }
    }

    /**
     * Checks if the given Player is the owner of the bed
     * 
     * @param player the Player who will be checked for ownership
     * @return true if the Player owns the bed
     */
    public boolean isOwner(String player) {
        return owner.equals(player);
    }
}

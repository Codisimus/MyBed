
package MyBed;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * @author Cody
 */
public class MyBedBlockListener extends BlockListener {
    protected static String permissionMessage;

    @Override
    public void onBlockBreak (BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        try {
            if (block.getType().equals(Material.SIGN))
                block.equals(block.getRelative(BlockFace.DOWN));
            if (block.getType().equals(Material.BED)) {
                OwnedBed bed = SaveSystem.getBed(player, block);
                if (bed.isOwner(player.getName()))
                    SaveSystem.removeBed(bed);
                else {
                    event.setCancelled(true);
                    player.sendMessage("That bed does not belong to you!");
                }
            }
        }
        catch (Exception ex) {
        }
    }
    
    @Override
    public void onSignChange (SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock().getRelative(BlockFace.DOWN);
        if (!block.getType().equals(Material.BED))
            return;
        OwnedBed bed = SaveSystem.getBed(player, block);
        if (!bed.isOwner(player.getName()) || !MyBed.hasPermission(player, "inn")) {
            event.setCancelled(true);
            player.sendMessage(permissionMessage);
        }
    }
}

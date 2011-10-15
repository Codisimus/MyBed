
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
        if (block.getType().equals(Material.WALL_SIGN))
            block = block.getRelative(BlockFace.DOWN);
        if (block.getType().equals(Material.BED_BLOCK)) {
            OwnedBed bed = SaveSystem.getBed(player, block);
            if (bed == null)
                return;
            if (bed.owner.equalsIgnoreCase(player.getName()) || MyBed.hasPermission(player, "admin")) {
                if (event.getBlock().getType().equals(Material.WALL_SIGN))
                    player.sendMessage("Inn Destroyed!");
                else {
                    System.out.println("Bed Removed");
                    SaveSystem.removeBed(bed);
                    SaveSystem.save();
                }
            }
            else {
                event.setCancelled(true);
                player.sendMessage(MyBedPlayerListener.notOwnerMessage);
            }
        }
    }
    
    @Override
    public void onSignChange (SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock().getRelative(BlockFace.DOWN);
        if (!block.getType().equals(Material.BED_BLOCK))
            return;
        OwnedBed bed = SaveSystem.getBed(player, block);
        if (!MyBed.hasPermission(player, "inn")) {
            event.getBlock().setTypeId(0);
            player.sendMessage(permissionMessage);
            return;
        }
        if (!bed.owner.equalsIgnoreCase(player.getName())) {
            event.getBlock().setTypeId(0);
            player.sendMessage(MyBedPlayerListener.notOwnerMessage);
            return;
        }
        player.sendMessage("Inn Made!");
    }
}

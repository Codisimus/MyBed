
package MyBed;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author Cody
 */
public class MyBedPlayerListener extends PlayerListener {
    protected static String notOwnerMessage;
    protected static String innMessage;

    @Override
    public void onPlayerBedEnter (PlayerBedEnterEvent event) {
        Block block = event.getBed();
        Player player = event.getPlayer();
        OwnedBed bed = SaveSystem.getBed(player, block);
        block = bed.head.getRelative(BlockFace.UP);
        if (bed == null)
            return;
        if (bed.isOwner(player.getName()))
            return;
        try {
            Sign sign = (Sign)block.getState();
            double cost = Double.parseDouble(sign.getLine(1).toLowerCase().replace("cost: ", ""));
            int health = Integer.parseInt(sign.getLine(2).toLowerCase().replace("health: ", ""));
            if (Register.charge(player, bed.owner, cost)) {
                player.sendMessage(innMessage.replaceAll("<cost>", Register.format(cost)).replaceAll("<health>", health+""));
                player.setHealth(player.getHealth()+health);
            }
            else
                event.setCancelled(true);
        }
        catch (Exception ex) {
            event.setCancelled(true);
            player.sendMessage(notOwnerMessage);
        }
    }
}

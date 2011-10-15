
package MyBed;

import java.util.LinkedList;
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
    protected static int maxHeals;
    private static LinkedList<User> users = new LinkedList<User>();

    @Override
    public void onPlayerBedEnter (PlayerBedEnterEvent event) {
        Block block = event.getBed();
        Player player = event.getPlayer();
        OwnedBed bed = SaveSystem.getBed(player, block);
        if (bed == null)
            return;
        if (bed.owner.equalsIgnoreCase(player.getName()))
            return;
        try {
            Sign sign = (Sign)bed.head.getRelative(BlockFace.UP).getState();
            double cost = Double.parseDouble(sign.getLine(1).toLowerCase().replace("cost:", "").replaceAll(" ", ""));
            int health = Integer.parseInt(sign.getLine(2).toLowerCase().replace("health:", "").replaceAll(" ", ""));
            User user = getUser(player.getName());
            if ((user.healed < maxHeals) || (maxHeals == -1)) {
                if (Register.charge(player, bed.owner, cost)) {
                    player.sendMessage(innMessage.replaceAll("<cost>", Register.format(cost)).replaceAll("<health>", health+""));
                    player.setHealth(player.getHealth()+health);
                    user.healed++;
                    return;
                }
            }
            else
                player.sendMessage("You cannot sleep in an Inn again tonight");
            event.setCancelled(true);
        }
        catch (Exception ex) {
            event.setCancelled(true);
            player.sendMessage(notOwnerMessage);
        }
    }
    
    /**
     * Returns the User object for the given player
     * Creates a User object if it doesn't exist
     * 
     * @param player The name of the Player
     * @return The User object for the given player
     */
    private User getUser(String player) {
        for (User user: users)
            if (user.name.equals(player))
                return user;
        User user = new User(player);
        users.add(user);
        return user;
    }
}

package com.codisimus.plugins.mybed.listeners;

import com.codisimus.plugins.mybed.OwnedBed;
import com.codisimus.plugins.mybed.Register;
import com.codisimus.plugins.mybed.SaveSystem;
import com.codisimus.plugins.mybed.User;
import java.util.LinkedList;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * Listens for Players sleeping in Beds/Inns
 *
 * @author Codisimus
 */
public class playerListener extends PlayerListener {
    public static String notOwnerMessage;
    public static String innMessage;
    public static int maxHeals;
    public static LinkedList<User> users = new LinkedList<User>();

    /**
     * Only allow Players to sleep in Beds if they are the Owner or the Bed is an Inn
     * 
     * @param event The PlayerBedEnterEvent that occurred
     */
    @Override
    public void onPlayerBedEnter (PlayerBedEnterEvent event) {
        Block block = event.getBed();
        Player player = event.getPlayer();
        
        //Return if the BLock is not an OwnedBed
        OwnedBed bed = SaveSystem.getBed(player, block);
        if (bed == null)
            return;
        
        //Return if the Player is the Owner
        if (bed.owner.equalsIgnoreCase(player.getName()))
            return;
        
        //Check if the Bed is an Inn
        try {
            Sign sign = (Sign)bed.head.getRelative(BlockFace.UP).getState();
            double cost = Double.parseDouble(sign.getLine(1).toLowerCase().replace("cost:", "").replaceAll(" ", ""));
            int health = Integer.parseInt(sign.getLine(2).toLowerCase().replace("health:", "").replaceAll(" ", ""));
            User user = getUser(player.getName());
            
            //Cancel the Event if the Player can not afford the Inn or maxed out their heals
            if ((user.healed < maxHeals) || (maxHeals == -1)) {
                if (Register.charge(player, bed.owner, cost)) {
                    player.setHealth(player.getHealth()+health);
                    user.healed++;
                    
                    player.sendMessage(innMessage.replaceAll("<cost>", Register.format(cost)).replaceAll("<health>", health+""));
                    return;
                }
            }
            else
                player.sendMessage("You cannot sleep in an Inn again tonight");
            
            event.setCancelled(true);
        }
        catch (Exception notInn) {
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
    public User getUser(String player) {
        //Find the User if it already exists
        for (User user: users)
            if (user.name.equals(player))
                return user;
        
        //Create a new User to return
        User user = new User(player);
        users.add(user);
        return user;
    }
}

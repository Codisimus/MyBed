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
        
        //Cancel the Event if there is not a Sign above the head of the Bed
        Block signBlock = bed.head.getRelative(BlockFace.UP);
        if (signBlock.getTypeId() != 323) {
            player.sendMessage(notOwnerMessage);
            event.setCancelled(true);
            return;
        }
        
        //Cancel the Event if the Bed is not an Inn
        Sign sign = (Sign)signBlock.getState();
        if (!sign.getLine(0).equalsIgnoreCase("inn")) {
            player.sendMessage(notOwnerMessage);
            event.setCancelled(true);
            return;
        }

        double cost = Double.parseDouble(sign.getLine(1).toLowerCase().replace("cost:", "").replaceAll(" ", ""));
        int health = Integer.parseInt(sign.getLine(2).toLowerCase().replace("health:", "").replaceAll(" ", ""));
        User user = getUser(player.getName());

        //Cancel the Event if the Player maxed out their heals
        if ((user.healed >= maxHeals) && (maxHeals != -1)) {
            player.sendMessage("You cannot sleep in an Inn again tonight");
            event.setCancelled(true);
            return;
        }

        //Cancel the Event if the Player can not afford the Inn
        if (Register.charge(player, bed.owner, cost)) {
            event.setCancelled(true);
            return;
        }

        //Heal the Player
        int newHealth = player.getHealth() + health;
        if (newHealth > 20)
            newHealth = 20;
        player.setHealth(newHealth);
        user.healed++;

        player.sendMessage(innMessage.replaceAll("<cost>", Register.format(cost)).replaceAll("<health>", health+""));
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

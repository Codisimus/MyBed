package com.codisimus.plugins.mybed.listeners;

import com.codisimus.plugins.mybed.Econ;
import com.codisimus.plugins.mybed.MyBed;
import com.codisimus.plugins.mybed.OwnedBed;
import com.codisimus.plugins.mybed.User;
import com.codisimus.plugins.mybed.listeners.BlockEventListener.Type;
import java.util.LinkedList;
import org.bukkit.Material;
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
public class PlayerEventListener extends PlayerListener {
    public static String notOwnerMsg;
    public static String innMsg;
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
        
        //Return if the Block is not an OwnedBed
        OwnedBed bed = MyBed.getBed(player, block);
        if (bed == null)
            return;
        
        //Return if the Player is the Owner
        if (bed.owner.equalsIgnoreCase(player.getName()))
            return;
        
        
        //Cancel the Event if there is not a Sign above the head of the Bed
        Block signBlock = bed.head.getRelative(BlockFace.UP);
        if (signBlock.getType() != Material.WALL_SIGN) {
            player.sendMessage(notOwnerMsg);
            event.setCancelled(true);
            return;
        }
        
        Sign sign = (Sign)signBlock.getState();
        Type type;

        //Cancel the Event if the Bed is not an Inn
        try {
            type = Type.valueOf(sign.getLine(0).replaceAll(" ", "_").toUpperCase());
        }
        catch (Exception notEnum) {
            player.sendMessage(notOwnerMsg);
            event.setCancelled(true);
            return;
        }
        
        boolean server;

        //Return if the Bed is a Guest Bed
        switch (type) {
            case INN: server = false; break;
            case SERVER_INN: server = true; break;
            default: return;
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
        if (!Econ.charge(player, bed.owner, cost, server)) {
            event.setCancelled(true);
            return;
        }

        //Heal the Player
        int newHealth = player.getHealth() + health;
        if (newHealth > 20)
            newHealth = 20;
        player.setHealth(newHealth);
        user.healed++;

        player.sendMessage(innMsg.replaceAll("<cost>", Econ.format(cost)).replaceAll("<health>", health+""));
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

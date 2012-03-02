package com.codisimus.plugins.mybed;

import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.Listener;

/**
 * Listens for Players Interacting with Beds/Inns
 *
 * @author Codisimus
 */
public class MyBedListener implements Listener {
    public static String notOwnerMsg;
    public static String innMsg;
    public static int maxHeals;
    public static LinkedList<User> users = new LinkedList<User>();
    static enum Type { INN, SERVER_INN, GUEST }
    public static String permissionMsg;

    /**
     * Only allow Players to sleep in Beds if they are the Owner or the Bed is an Inn
     * 
     * @param event The PlayerBedEnterEvent that occurred
     */
    @EventHandler
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
     * Only Allows Owners and Admins to destroy OwnedBeds
     * 
     * @param event The BlockBreakEvent that occurred
     */
    @EventHandler
    public void onBlockBreak (BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        //If the Block is a Sign, check the Block below it
        Sign sign = null;
        switch (block.getType()) {
            case WALL_SIGN:
                //Return if the Sign is not a MyBed Sign
                try {
                    sign = (Sign)block.getState();
                    Type.valueOf(sign.getLine(0).toUpperCase().replaceAll(" ", "_"));

                    block = block.getRelative(BlockFace.DOWN);
                }
                catch (Exception notEnum) {
                    return;
                }
                
                //Fall through
                
            case BED_BLOCK:
                //Return if the Bed is not owned
                OwnedBed bed = MyBed.getBed(player, block);
                if (bed == null)
                    return;
                
                //Cancel the Event if the Player is not the Owner nor an Admin
                if (!bed.owner.equalsIgnoreCase(player.getName()) && !MyBed.hasPermission(player, "admin")) {
                    event.setCancelled(true);
                    player.sendMessage(MyBedListener.notOwnerMsg);
                    return;
                }
                
                if (sign != null)
                    player.sendMessage("Inn Destroyed!");
                else {
                    MyBed.removeBed(player.getWorld().getName(), bed);
                    
                    try {
                        //Return if there is not a Sign above the head of the Bed
                        sign = (Sign)bed.head.getRelative(BlockFace.UP).getState();
                        
                        //Return if the Sign is not a MyBed Sign
                        Type.valueOf(sign.getLine(0).toUpperCase().replaceAll(" ", "_"));
                        
                        //Clear the Sign
                        for (int i = 0; i < 4; i++)
                            sign.setLine(i, "");
                        sign.update();
                    }
                    catch (Exception notInn) {
                    }
                }
        }
    }
    
    /**
     * Listens for Players creating Inns
     * 
     * @param event The SignChangeEvent that occurred
     */
    @EventHandler
    public void onSignChange (SignChangeEvent event) {
        String line = event.getLine(0).toLowerCase().replaceAll(" ", "_");
        Type type;

        //Return if the first line is not 'inn' or 'guest'
        try {
            type = Type.valueOf(line.toUpperCase());
        }
        catch (Exception notEnum) {
            return;
        }

        Block signBlock = event.getBlock();
        Player player = event.getPlayer();

        //Cancel if the Player does not have permission to create Inns
        if (!MyBed.hasPermission(player, line)) {
            event.setCancelled(true);
            player.sendMessage(permissionMsg);
            return;
        }

        switch (type) {
            case SERVER_INN: //Fall through
            case INN: //Verify the format of the Sign
                try {
                    Double.parseDouble(event.getLine(1).toLowerCase().replace("cost:", "").replaceAll(" ", ""));
                    Integer.parseInt(event.getLine(2).toLowerCase().replace("health:", "").replaceAll(" ", ""));
                }
                catch (Exception invalidFormat) {
                    event.setCancelled(true);
                    player.sendMessage("Invalid format of Inn sign, correct format is...");
                    player.sendMessage("      Inn");
                    player.sendMessage("    cost: 50");
                    player.sendMessage("   health: 10");
                    player.sendMessage("  Any text want");
                    return;
                }
                
            default: break;
        }

        //Cancel the event if the Sign is not above the head of a Bed
        Block bedBlock = signBlock.getRelative(BlockFace.DOWN);
        if (bedBlock.getType() != Material.BED_BLOCK) {
            event.setCancelled(true);
            player.sendMessage("A MyBed sign can only be placed above the head of a Bed");
            return;
        }

        //Return if the Bed is not Owned
        OwnedBed bed = MyBed.getBed(player, bedBlock);
        if (bed == null) {
            event.setCancelled(true);
            return;
        }

        //Cancel if the Player is not the Owner or an Admin
        if (!bed.owner.equalsIgnoreCase(player.getName()) && !MyBed.hasPermission(player, "admin")) {
            event.setCancelled(true);
            player.sendMessage(MyBedListener.notOwnerMsg);
            return;
        }
        
        switch (type) {
            case SERVER_INN:
                bed.owner = "server";
                MyBed.save(player.getWorld().getName());
                
                player.sendMessage("Server Inn Created!");
                break;
                
            case INN: player.sendMessage("Inn Created!"); break;
            case GUEST: player.sendMessage("Guest Bed Created!"); break;
            default: break;
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

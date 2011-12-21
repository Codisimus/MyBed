package com.codisimus.plugins.mybed.listeners;

import com.codisimus.plugins.mybed.MyBed;
import com.codisimus.plugins.mybed.OwnedBed;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Listens for griefing events and Inn creating
 *
 * @author Codisimus
 */
public class BlockEventListener extends BlockListener {
    static enum Type { INN, SERVER_INN, GUEST }
    public static String permissionMsg;

    /**
     * Only Allows Owners and Admins to destroy OwnedBeds
     * 
     * @param event The BlockBreakEvent that occurred
     */
    @Override
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
                    player.sendMessage(PlayerEventListener.notOwnerMsg);
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
                    
                return;
                
            default: return; //Return if the Block is not a Bed
        }
    }
    
    /**
     * Listens for Players creating Inns
     * 
     * @param event The SignChangeEvent that occurred
     */
    @Override
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
            player.sendMessage(PlayerEventListener.notOwnerMsg);
            return;
        }
        
        switch (type) {
            case SERVER_INN:
                bed.owner = "server";
                MyBed.save(player.getWorld().getName());
                
                player.sendMessage("Server Inn Created!");
                return;
                
            case INN: player.sendMessage("Inn Created!"); return;
            case GUEST: player.sendMessage("Guest Bed Created!"); return;
            default: return;
        }
    }
}

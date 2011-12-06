package com.codisimus.plugins.mybed.listeners;

import com.codisimus.plugins.mybed.MyBed;
import com.codisimus.plugins.mybed.OwnedBed;
import com.codisimus.plugins.mybed.SaveSystem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Listens for griefing events and Inn creating
 *
 * @author Codisimus
 */
public class blockListener extends BlockListener {
    public static String permissionMessage;

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
        if (block.getTypeId() == 68)
            block = block.getRelative(BlockFace.DOWN);
        
        //Return if the Block is not a Bed
        if (block.getTypeId() != 26)
            return;
        
        //Return if the Bed is not owned
        OwnedBed bed = SaveSystem.getBed(player, block);
        if (bed == null)
            return;
        
        //Check if the Player is the Owner or an Admin
        if (bed.owner.equalsIgnoreCase(player.getName()) || MyBed.hasPermission(player, "admin")) {
            if (event.getBlock().getType().equals(Material.WALL_SIGN))
                player.sendMessage("Inn Destroyed!");
            else {
                SaveSystem.removeBed(player.getWorld().getName(), bed);
                System.out.println("Bed Removed");
            }
        }
        else {
            event.setCancelled(true);
            player.sendMessage(playerListener.notOwnerMessage);
        }
    }
    
    /**
     * Listens for Players creating Inns
     * 
     * @param event The SignChangeEvent that occurred
     */
    @Override
    public void onSignChange (SignChangeEvent event) {
        //Return if the first line is not 'inn'
        if (!event.getLine(0).equalsIgnoreCase("inn"))
            return;

        Block signBlock = event.getBlock();
        Player player = event.getPlayer();

        //Cancel if the Player does not have permission to create Inns
        if (!MyBed.hasPermission(player, "inn")) {
            event.setCancelled(true);
            player.sendMessage(permissionMessage);
            return;
        }

        //Verify the format of the sign
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

        //Cancel the event if the Block under the Sign is not a Bed
        Block bedBlock = signBlock.getRelative(BlockFace.DOWN);
        if (bedBlock.getTypeId() != 26) {
            event.setCancelled(true);
            player.sendMessage("An Inn sign can only be placed above the head of a Bed");
            return;
        }

        //Return if the Bed is not Owned
        OwnedBed bed = SaveSystem.getBed(player, bedBlock);
        if (bed == null) {
            event.setCancelled(true);
            return;
        }

        //Cancel if the Player is not the Owner or an Admin
        if (!bed.owner.equalsIgnoreCase(player.getName()) && MyBed.hasPermission(player, "admin")) {
            event.setCancelled(true);
            player.sendMessage(playerListener.notOwnerMessage);
            return;
        }

        //Cancel the event if the Sign is not above the head of the Bed
        if (!bed.head.equals(bedBlock)) {
            event.setCancelled(true);
            player.sendMessage("An Inn sign can only be placed above the head of a Bed");
            return;
        }
        
        player.sendMessage("Inn Created!");
    }
}

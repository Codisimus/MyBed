package com.codisimus.plugins.mybed;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

/**
 * Manages payment/rewards of using Warps
 * 
 * @author Codisimus
 */
public class Econ {
    public static Economy economy;
    public static String insufficientFunds;

    /**
     * Charges a Player a given amount of money, which goes to a Player/Bank
     * 
     * @param player The name of the Player to be charged
     * @param source The Player/Bank that will receive the money
     * @param amount The amount that will be charged
     * @param server True if the money is going to the Server
     * @return True if the transaction was successful
     */
    public static boolean charge(Player player, String owner, double amount, boolean server) {
        String name = player.getName();
        
        //Cancel if the Player cannot afford the transaction
        if (!economy.has(name, amount)) {
            player.sendMessage(insufficientFunds.replaceAll("<cost>", ""+economy.format(amount)));
            return false;
        }
        
        economy.withdrawPlayer(name, amount);
        
        //Send money to the Owner if it is not a Server Inn
        if (!server)
            economy.depositPlayer(owner, amount);
        
        return true;
    }
    
    /**
     * Formats the money amount by adding the unit
     * 
     * @param amount The amount of money to be formatted
     * @return The String of the amount + currency name
     */
    public static String format(double amount) {
        return economy.format(amount).replace(".00", "");
    }
}

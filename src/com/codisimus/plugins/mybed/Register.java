package com.codisimus.plugins.mybed;

import com.codisimus.plugins.mybed.register.payment.Method;
import com.codisimus.plugins.mybed.register.payment.Method.MethodAccount;
import org.bukkit.entity.Player;

/**
 * Manages payment for buying Beds
 * Using Nijikokun's Register API
 *
 * @author Codisimus
 */
public class Register {
    public static String economy;
    public static Method econ;
    public static String insufficientFunds;

    /**
     * Subtracts a specific amount from the players total balance
     * Returns true if the transaction was successful
     * 
     * @param payer The Player who was is being charged money
     * @param amount The amount which will be charged
     * @param type The type of block
     * @return true if the transaction was successful
     */
    public static boolean charge(Player payer, String owner, double amount) {
        MethodAccount account = econ.getAccount(payer.getName());
        
        //Return false if the Player has insufficient funds
        if (!account.hasEnough(amount)) {
            payer.sendMessage(insufficientFunds.replaceAll("<cost>", ""+format(amount)));
            return false;
        }
        
        account.subtract(amount);
        account = econ.getAccount(owner);
        account.add(amount);
        return true;
    }
    
    /**
     * Formats the money amount by adding the unit
     * 
     * @param amount The amount of money to be formatted
     * @return The String of the amount + currency name
     */
    public static String format(double amount) {
        return econ.format(amount);
    }
}

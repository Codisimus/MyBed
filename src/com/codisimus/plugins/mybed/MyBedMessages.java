package com.codisimus.plugins.mybed;

/**
 * Holds messages that are displayed to users of this plugin
 *
 * @author Codisimus
 */
public class MyBedMessages {
    static String permission;
    static String insufficientFunds;
    static String inn;
    static String notOwner;
    
    /**
     * Formats all Turnstile messages
     * 
     */
    static void formatAll() {
        permission = format(permission);
        insufficientFunds = format(insufficientFunds);
        inn = format(inn);
        notOwner = format(notOwner);
    }
    
    /**
     * Adds various Unicode characters and colors to a string
     * 
     * @param string The string being formated
     * @return The formatted String
     */
    private static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }
}
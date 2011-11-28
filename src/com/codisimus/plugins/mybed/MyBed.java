package com.codisimus.plugins.mybed;

import com.codisimus.plugins.mybed.listeners.worldListener;
import com.codisimus.plugins.mybed.listeners.playerListener;
import com.codisimus.plugins.mybed.listeners.blockListener;
import com.codisimus.plugins.mybed.listeners.pluginListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;

/**
 * Loads Plugin and manages Permissions
 *
 * @author Codisimus
 */
public class MyBed extends JavaPlugin {
    public static Server server;
    public static PermissionManager permissions;
    public static PluginManager pm;
    public Properties p;

    @Override
    public void onDisable() {
    }

    /**
     * Calls methods to load this Plugin when it is enabled
     *
     */
    @Override
    public void onEnable() {
        server = getServer();
        pm = server.getPluginManager();
        checkFiles();
        for (World loadedWorld: server.getWorlds())
            SaveSystem.load(loadedWorld);
        loadConfig();
        registerEvents();
        System.out.println("MyBed "+this.getDescription().getVersion()+" is enabled!");
    }

    /**
     * Makes sure all needed files exist
     *
     */
    public void checkFiles() {
        if (!new File("plugins/MyBed/config.properties").exists())
            moveFile("config.properties");
    }

    /**
     * Moves file from MyBed.jar to appropriate folder
     * Destination folder is created if it doesn't exist
     *
     * @param fileName The name of the file to be moved
     */
    public void moveFile(String fileName) {
        try {
            //Retrieve file from this plugin's .jar
            JarFile jar = new JarFile("plugins/MyBed.jar");
            ZipEntry entry = jar.getEntry(fileName);
            
            //Create the destination folder if it does not exist
            String destination = "plugins/MyBed/";
            File file = new File(destination.substring(0, destination.length()-1));
            if (!file.exists())
                file.mkdir();
            
            //Copy the file
            File efile = new File(destination, fileName);
            InputStream in = new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(efile));
            byte[] buffer = new byte[2048];
            while (true) {
                int nBytes = in.read(buffer);
                if (nBytes <= 0)
                    break;
                out.write(buffer, 0, nBytes);
            }
            out.flush();
            out.close();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads settings from the config.properties file
     *
     */
    public void loadConfig() {
        p = new Properties();
        try {
            p.load(new FileInputStream("plugins/MyBed/config.properties"));
        }
        catch (Exception e) {
        }
        Register.economy = loadValue("Economy");
        pluginListener.useBP = Boolean.parseBoolean(loadValue("UseBukkitPermissions"));
        playerListener.maxHeals = Integer.parseInt(loadValue("MaxHealsPerNight"));
        
        Register.insufficientFunds = format(loadValue("InsufficientFundsMessage"));
        playerListener.innMessage = format(loadValue("InnMessage"));
        playerListener.notOwnerMessage = format(loadValue("NotOwnerMessage"));
        blockListener.permissionMessage = format(loadValue("PermissionMessage"));
    }

    /**
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    public String loadValue(String key) {
        //Print an error if the key is not found
        if (!p.containsKey(key)) {
            System.err.println("[MyBed] Missing value for "+key+" in config file");
            System.err.println("[MyBed] Please regenerate config file");
        }
        
        return p.getProperty(key);
    }
    
    /**
     * Registers events for the MyBed Plugin
     *
     */
    public void registerEvents() {
        blockListener blockListener = new blockListener();
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, new pluginListener(), Priority.Monitor, this);
        pm.registerEvent(Type.WORLD_LOAD, new worldListener(), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_BED_ENTER, new playerListener(), Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
    }

    /**
     * Returns boolean value of whether the given player has the specific permission
     *
     * @param player The Player who is being checked for permission
     * @param type The String of the permission, ex. inn
     * @return true if the given player has the specific permission
     */
    public static boolean hasPermission(Player player, String type) {
        //Check if a Permission Plugin is present
        if (permissions != null)
            return permissions.has(player, "mybed."+type);
        
        //Return Bukkit Permission value
        return player.hasPermission("mybed."+type);
    }
    
    /**
     * Adds various Unicode characters and colors to a string
     * 
     * @param string The string being formated
     * @return The formatted String
     */
    public static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }
}

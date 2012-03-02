package com.codisimus.plugins.mybed;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads Plugin and manages Data/Permissions
 *
 * @author Codisimus
 */
public class MyBed extends JavaPlugin {
    private static Server server;
    private static Permission permission;
    private static PluginManager pm;
    private Properties p;
    private static HashMap<String, LinkedList<OwnedBed>> beds = new HashMap<String, LinkedList<OwnedBed>>();
    private static Plugin plugin;
    private static String dataFolder;

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
        plugin = this;
        
        File dir = this.getDataFolder();
        if (!dir.isDirectory())
            dir.mkdir();
        
        dataFolder = dir.getPath();
        
        dir = new File(dataFolder+"/Beds");
        if (!dir.isDirectory())
            dir.mkdir();
        
        try {
            //Copy the file from the jar if it is missing
            File file = new File(dataFolder+"/config.properties");
            if (!file.exists())
                this.saveResource("config.properties", true);
            
            //Load config file
            p = new Properties();
            FileInputStream fis = new FileInputStream(file);
            p.load(fis);
            
            MyBedListener.maxHeals = Integer.parseInt(loadValue("MaxHealsPerNight"));

            MyBedMessages.insufficientFunds = loadValue("InsufficientFundsMessage");
            MyBedMessages.inn = loadValue("InnMessage");
            MyBedMessages.notOwner = loadValue("NotOwnerMessage");
            MyBedMessages.permission = loadValue("PermissionMessage");
            MyBedMessages.formatAll();
            
            fis.close();
        }
        catch (Exception missingProp) {
            System.err.println("Failed to load MyBed "+this.getDescription().getVersion());
            missingProp.printStackTrace();
        }
        
        //Load Beds Data
        loadAll();
        
        //Find Permissions
        RegisteredServiceProvider<Permission> permissionProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
            permission = permissionProvider.getProvider();
        
        //Find Economy
        RegisteredServiceProvider<Economy> economyProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
            Econ.economy = economyProvider.getProvider();
        
        //Register Events
        pm.registerEvents(new MyBedListener(), this);
        
        System.out.println("MyBed "+this.getDescription().getVersion()+" is enabled!");
    }

    /**
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    private String loadValue(String key) {
        //Print an error if the key is not found
        if (!p.containsKey(key)) {
            System.err.println("[MyBed] Missing value for "+key+" in config file");
            System.err.println("[MyBed] Please regenerate config file");
        }
        
        return p.getProperty(key);
    }

    /**
     * Returns boolean value of whether the given player has the specific permission
     *
     * @param player The Player who is being checked for permission
     * @param node The String of the permission, ex. inn
     * @return true if the given player has the specific permission
     */
    public static boolean hasPermission(Player player, String node) {
        return permission.has(player, "mybed."+node);
    }
    
    /*
     * Loads data for all Worlds
     * 
     */
    public static void loadAll() {
        File[] files = plugin.getDataFolder().listFiles();

        //Organize files
        if (files != null)
            for (File file: files) {
                String name = file.getName();
                if (name.endsWith(".dat")) {
                    File dest = new File(dataFolder+"/Beds/"+name);
                    file.renameTo(dest);
                }
            }
        
        for (World loadedWorld: server.getWorlds())
            loadData(loadedWorld);
    }
    
    /**
     * Reads save file to load MyBed data
     * Only loads the data off the specified World
     */
    public static void loadData(World world) {
        String worldName = world.getName();

        //Open save file for the Chest data of the given World
        File file = new File(dataFolder+"/Beds/"+worldName+".dat");
        if (file.exists())
            try {
                //Open save file for loading data
                BufferedReader bReader = new BufferedReader(new FileReader(file));

                LinkedList<OwnedBed> bedList = new LinkedList<OwnedBed>();

                String line;
                while ((line = bReader.readLine()) != null)
                    try {
                        String[] split = line.split(";");

                        String owner = split[0];
                        int x = Integer.parseInt(split[1]);
                        int y = Integer.parseInt(split[2]);
                        int z = Integer.parseInt(split[3]);
                        Block block = world.getBlockAt(x, y, z);

                        if (block.getTypeId() == 26)
                            bedList.add(new OwnedBed(owner, block));
                    }
                    catch (Exception corruptedData) {
                        /* Do not load this line */
                    }

                beds.put(worldName, bedList);
                bReader.close();
            }
            catch (Exception loadFailed) {
                System.err.println("[MyBed] Load failed");
                loadFailed.printStackTrace();
            }
    }

    /**
     * Writes data to save file
     * Old file is overwritten
     */
    public static void save(String world) {
        try {
            //Create save file if it doesn't exist
            File file = new File(dataFolder+"/Beds/"+world+".dat");
            if (!file.exists())
                file.createNewFile();
            
            //Open save file for writing data
            BufferedWriter bWriter = new BufferedWriter(new FileWriter(file));

            for (OwnedBed bed: beds.get(world)) {
                bWriter.write(bed.owner.concat(";"));
                Block block = bed.head;
                bWriter.write(block.getX()+";");
                bWriter.write(block.getY()+";");
                bWriter.write(block.getZ()+";");

                //Write each Bed on it's own line
                bWriter.newLine();
            }

            bWriter.close();
        }
        catch (Exception ex) {
        }
    }
    
    /**
     * Returns the OwnedBed which is located at the given block
     * Creates a new OwnedBed if one doesn't exist and they have permission
     * 
     * @param player the owner of the OwnedBed if a new one is created
     * @param block the given block
     * @return the OwnedBed which is located at the given block
     */
    public static OwnedBed getBed(Player player, Block block) {
        //Find OwnedBed in HashMap
        String world = block.getWorld().getName();
        LinkedList<OwnedBed> bedList = beds.get(world);
        for (OwnedBed bed: bedList)
            if (bed.head.equals(block) || bed.foot.equals(block))
                return bed;
        
        //Give ownership of the Bed to the Player if they are allowed
        if (MyBed.hasPermission(player, "own")) {
            OwnedBed bed = new OwnedBed(player.getName(), block);
            bedList.add(bed);
            save(world);
            return bed;
        }
        
        return null;
    }

    /**
     * Adds the given OwnedBed to the HasMap of OwnedBeds
     *
     * @param world The name of the World the OwnedBed is in
     * @param bed The given OwnedBed
     */
    public static void addBed(String world, OwnedBed bed) {
        beds.get(world).add(bed);
        save(world);
    }

    /**
     * Removes the given OwnedBed from the HasMap of OwnedBeds
     *
     * @param world The name of the World the OwnedBed is in
     * @param bed The given OwnedBed
     */
    public static void removeBed(String world, OwnedBed bed) {
        beds.get(world).remove(bed);
        save(world);
    }
}

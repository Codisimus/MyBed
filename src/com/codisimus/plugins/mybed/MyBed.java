package com.codisimus.plugins.mybed;

import com.codisimus.plugins.mybed.listeners.WorldLoadListener;
import com.codisimus.plugins.mybed.listeners.PlayerEventListener;
import com.codisimus.plugins.mybed.listeners.BlockEventListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
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
    private static HashMap beds = new HashMap();

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
        
        //Load Config settings
        p = new Properties();
        try {
            //Copy the file from the jar if it is missing
            if (!new File("plugins/MyBed/config.properties").exists())
                moveFile("config.properties");
            
            FileInputStream fis = new FileInputStream("plugins/MyBed/config.properties");
            p.load(fis);
            
            PlayerEventListener.maxHeals = Integer.parseInt(loadValue("MaxHealsPerNight"));

            Econ.insufficientFunds = format(loadValue("InsufficientFundsMessage"));
            PlayerEventListener.innMsg = format(loadValue("InnMessage"));
            PlayerEventListener.notOwnerMsg = format(loadValue("NotOwnerMessage"));
            BlockEventListener.permissionMsg = format(loadValue("PermissionMessage"));
            
            fis.close();
        }
        catch (Exception missingProp) {
            System.err.println("Failed to load MyBed "+this.getDescription().getVersion());
            missingProp.printStackTrace();
        }
        
        //Load Beds Data
        for (World loadedWorld: server.getWorlds())
            loadData(loadedWorld);
        
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
        BlockEventListener blockListener = new BlockEventListener();
        pm.registerEvent(Type.WORLD_LOAD, new WorldLoadListener(), Priority.Monitor, this);
        pm.registerEvent(Type.PLAYER_BED_ENTER, new PlayerEventListener(), Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
        
        System.out.println("MyBed "+this.getDescription().getVersion()+" is enabled!");
    }

    /**
     * Moves file from MyBed.jar to appropriate folder
     * Destination folder is created if it doesn't exist
     *
     * @param fileName The name of the file to be moved
     */
    private void moveFile(String fileName) {
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
    
    /**
     * Reads save file to load MyBed data
     * Only loads the data off the specified World
     */
    public static void loadData(World world) {
        String line = "";
        String worldName = world.getName();

        try {
            //Create save file if it doesn't exist
            new File("plugins/MyBed/"+worldName+".dat").createNewFile();

            //Open save file for loading data
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/MyBed/"+worldName+".dat"));

            LinkedList<OwnedBed> bedList = new LinkedList<OwnedBed>();

            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(";");

                String owner = split[0];
                int x = Integer.parseInt(split[1]);
                int y = Integer.parseInt(split[2]);
                int z = Integer.parseInt(split[3]);
                Block block = world.getBlockAt(x, y, z);

                if (block.getTypeId() == 26)
                    bedList.add(new OwnedBed(owner, block));
            }

            beds.put(worldName, bedList);
            bReader.close();
            if (bedList.isEmpty())
                loadOld(world);
        }
        catch (Exception loadFailed) {
            System.err.println("[MyBed] Load failed for "+worldName+".dat, Errored line: "+line);
            loadFailed.printStackTrace();
        }
    }

    /**
     * Reads outdated save file to load MyBed data
     * Only loads the data off the specified World
     */
    private static void loadOld(World world) {
        String line = "";
        String worldName = world.getName();
        
        try {
            //Cancel if the file doesn't exist
            if (!new File("plugins/MyBed/mybed.save").exists())
                return;

            //Open save file for loading data
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/MyBed/mybed.save"));

            LinkedList<OwnedBed> bedList = new LinkedList<OwnedBed>();

            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(";");
                
                //Update outdated save file
                if (split[1].endsWith("~NETHER"))
                    split[1].replace("~NETHER", "");
                
                if (worldName.equals(split[1])) {
                    String owner = split[0];
                    int x = Integer.parseInt(split[2]);
                    int y = Integer.parseInt(split[3]);
                    int z = Integer.parseInt(split[4]);
                    Block block = world.getBlockAt(x, y, z);
                    
                    if (block.getTypeId() == 26)
                        bedList.add(new OwnedBed(owner, block));
                }
            }

            beds.put(worldName, bedList);
            save(worldName);
            bReader.close();
        }
        catch (Exception loadFailed) {
            System.err.println("[MyBed] Load failed for "+worldName+".dat, Errored line: "+line);
            loadFailed.printStackTrace();
        }
    }

    /**
     * Writes data to save file
     * Old file is overwritten
     */
    public static void save(String world) {
        try {
            //Open save file for writing data
            BufferedWriter bWriter = new BufferedWriter(new FileWriter("plugins/MyBed/"+world+".dat"));

            for (OwnedBed bed: (LinkedList<OwnedBed>)beds.get(world)) {
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
        LinkedList<OwnedBed> bedList = (LinkedList<OwnedBed>)beds.get(world);
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
        ((LinkedList<OwnedBed>)beds.get(world)).add(bed);
        save(world);
    }

    /**
     * Removes the given OwnedBed from the HasMap of OwnedBeds
     *
     * @param world The name of the World the OwnedBed is in
     * @param bed The given OwnedBed
     */
    public static void removeBed(String world, OwnedBed bed) {
        ((LinkedList<OwnedBed>)beds.get(world)).remove(bed);
        save(world);
    }
}

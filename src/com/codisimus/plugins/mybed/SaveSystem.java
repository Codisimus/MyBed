package com.codisimus.plugins.mybed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Holds MyBed data and is used to load/save data
 *
 * @author Codisimus
 */
public class SaveSystem {
    public static HashMap beds = new HashMap();
    
    /**
     * Reads save file to load MyBed data
     * Only loads the data off the specified World
     */
    public static void load(World world) {
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
    public static void loadOld(World world) {
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
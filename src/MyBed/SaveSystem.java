
package MyBed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author Cody
 */
class SaveSystem {
    public static LinkedList<OwnedBed> beds = new LinkedList<OwnedBed>();
    
    /**
     * Reads save file to load MyBed data
     *
     */
    public static void load() {
        try {
            new File("plugins/MyBed").mkdir();
            new File("plugins/MyBed/mybed.save").createNewFile();
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/MyBed/mybed.save"));
            String line = "";
            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(";");
                String owner = split[0];
                int x = Integer.parseInt(split[2]);
                int y = Integer.parseInt(split[3]);
                int z = Integer.parseInt(split[4]);
                World world;
                Block block;
                boolean nether = false;
                if (split[1].endsWith("~NETHER")) {
                    nether = true;
                    split[1] = split[1].replace("~NETHER", "");
                }
                try {
                    world = MyBed.server.getWorld(split[1]);
                    block = world.getBlockAt(x, y, z);
                }
                catch (NullPointerException newWorld) {
                    if (nether)
                        world = MyBed.server.createWorld(split[1], World.Environment.NETHER);
                    else
                        world = MyBed.server.createWorld(split[1], World.Environment.NORMAL);
                    block = world.getBlockAt(x, y, z);
                }
                if (block.getTypeId() == 26) {
                    beds.add(new OwnedBed(owner, block));
                }
            }
        }
        catch (Exception ex) {
        }
    }

    /**
     * Writes data to save file
     * Old file is overwritten
     */
    public static void save() {
        try {
            BufferedWriter bWriter = new BufferedWriter(new FileWriter("plugins/MyBed/mybed.save"));
            for(OwnedBed bed : beds) {
                bWriter.write(bed.owner.concat(";"));
                Block block = bed.head;
                World world = block.getWorld();
                if (world.getEnvironment().equals(Environment.NETHER))
                    bWriter.write(block.getWorld().getName()+"~NETHER;");
                else
                    bWriter.write(block.getWorld().getName()+";");
                bWriter.write(block.getX()+";");
                bWriter.write(block.getY()+";");
                bWriter.write(block.getZ()+";");
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
        for (OwnedBed bed : beds) {
            if (bed.head.equals(block) || bed.foot.equals(block))
                return bed;
        }
        if (MyBed.hasPermission(player, "own")) {
            OwnedBed bed = new OwnedBed(player.getName(), block);
            beds.add(bed);
            save();
            return bed;
        }
        return null;
    }

    /**
     * Deletes the given bed
     * 
     * @param bed the OwnedBed that will be removed
     */
    public static void removeBed(OwnedBed bed) {
        beds.remove(bed);
        save();
    }
}
package tf.tuff.API.TuffXAPI;

import org.bukkit.plugin.java.JavaPlugin;
import tf.tuff.ChunkSectionKey;
import tf.tuff.LegacyBlockIds;
import tf.tuff.ServerRegistry;
import tf.tuff.TuffX;

import java.util.Collections;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;

import static tf.tuff.LegacyBlockIds.BLOCK_ID_MAP;
import static tf.tuff.LegacyBlockIds.unmapped;

/* TuffX API for any addons 
 * author@turbomaxe
 * since 1/16/2026
 */
public class TuffxAPI {
    private static TuffxAPI instance = null;
    
    public static final String version = "1.2.0";

    /**
    * only the api can make an 
    * instance of the plugin
    */
    public TuffxAPI() {}

    /**
    * uses the constructor of that method
    * to retrive the section key
    * @return a new chunksectionkey object with the constructors
    */
    public static ChunkSectionKey getChunkSectionKey(
            UUID player,
            String world,
            int x,
            int z,
            int y
    ) {
        return new ChunkSectionKey(player, world, x, z, y);
    }

    /**
    * gets the instance, throws
    * an error if there isn't one
    * @return the current instance if there is one
    */
    public static TuffxAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "The TuffX API is not initialized yet!"
            );
        }
        return instance;
    }

    /**
    * retrives the server registry
    *
    * @param pl our java plugin
    */

    public ServerRegistry getServerRegistry(JavaPlugin pl, String registryUrl, String serverAddr) {
        return new ServerRegistry(pl, registryUrl, serverAddr);
    }

    public static Map<String, Integer> getBlockIDMap() {
        return Collections.unmodifiableMap(BLOCK_ID_MAP);
    }

    /**
    * setter method to make an
    * instance
    *
    * @param newInstance the instance that will be made
    */

    public static void setInstance(TuffxAPI newInstance) {
        if (instance != null) {
            throw new IllegalStateException("TuffxAPI instance has already been set!");
        }
        instance = newInstance;
    }

    /**
    * returns the name of a certain block
    * using the id
    *
    * @param id the id of the block
    */
    public static Integer getBlockName(int id) {
        return BLOCK_ID_MAP.get(id);
    }

    /**
    * @return the version of TuffX
    */
    public static String getVersion() {
        return version;
    }

    /**
    * returns the id of a certain block
    * using the name
    *
    * @param name the title of the block (ex. "redstone_lamp")
    * @return the block id map by name
    */

    public static int getBlockID(String name) {
        return BLOCK_ID_MAP.getOrDefault(name, 1);
    }
    /**
    * @return the entire block ID map
    */
   
    public Map<String, Integer> getEntireMap() {
        return BLOCK_ID_MAP;
    }

    /**
    * Clears the map, useful for creating new 
    * maps
    */

    public Map<String,Integer> clearEntireMap() { BLOCK_ID_MAP.clear(); return BLOCK_ID_MAP;
    }

    /**
    * gets the entire unmapped set
    * @return the unmapped block hashset
    */
   
    public static final Set<String> getUnmappedSet() { return unmapped; }

    /**
    * clears the unmapped hashset with clear();
    *  @return the cleared hashset
    */
    
    public static final Set<String> clearUnmappedSet() { unmapped.clear(); return unmapped; }

}



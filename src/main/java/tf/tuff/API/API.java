package tf.tuff.TuffxAPI;

import tf.tuff.ChunkSectionKey;
import tf.tuff.LegacyBlockIds;
import tf.tuff.TuffX;

import java.util.Collections;
import java.util.Map;

/* TuffX API for any addons 
 * author@turbomaxe
 * since 1/16/2026
 */
public class TuffxAPI {
    private static TuffxAPI instance = null

  public static final String version = "1.2.0";
  
   public TuffxAPI() {}

  public static ChunkSectionKey getChunkSectionKey() {
    return new ChunkSectionKey(); 
    }

    public static TuffxAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "The TuffX API is not initialized yet!"
                  .stripIndent());
        }
        return instance;
    }
  
  public static Map<String, Integer> getBlockIDMap() {
    return Collections.unmodifiableMap(BlockRegistry.BLOCK_ID_MAP);
    }

    public static void setInstance(TuffxAPI newInstance) {
        if (instance != null) {
            throw new IllegalStateException("TuffxAPI instance has already been set!");
        }
        instance = newInstance;
    }
  
  public static Integer getBlockId(String name) {
    return BlockRegistry.BLOCK_ID_MAP.get(name);
    }

  public static String getVersion() {
    return version;    
    }
  
}   




package tf.tuff.API;

import tf.tuff.ChunkSectionKey;
import tf.tuff.LegacyBlockIds;
import tf.tuff.TuffX;

import java.util.Collections;
import java.util.Map;

/* TuffX API for any addons 
 * author@turbomaxe
 * since 1/16/2026
 */
public class API {

  public static final String version = "1.2.0";
  
   public API() {}

  public static ChunkSectionKey getChunkSectionKey() {
    return new ChunkSectionKey(); 
    }
  public static Map<String, Integer> getBlockIDMap() {
    return Collections.unmodifiableMap(BlockRegistry.BLOCK_ID_MAP);
    }
  
  public static Integer getBlockId(String name) {
    return BlockRegistry.BLOCK_ID_MAP.get(name);
    }

  public static String getVersion() {
    return version;    
    }
  
}   




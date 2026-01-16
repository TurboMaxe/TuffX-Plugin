package tf.tuff.API;

import tf.tuff.ChunkSectionKey;
import tf.tuff.LegacyBlockIds;
import tf.tuff.TuffX;


/* TuffX API for any addons 
 * author@turbomaxe
 * since 1/16/2026
 */
public class API {

  public ChunkSectionKey getChunkSectionKey() {
    return ChunkSectionKey; 
    }
  public static Map<String, Integer> getBlockIDMap() {
    return Collections.unmodifiableMap(BlockRegistry.BLOCK_ID_MAP);
    }
  
  public static Integer getBlockId(String name) {
    return BlockRegistry.BLOCK_ID_MAP.get(name);
    }

   public static AuctionHouseAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
    }   
     public static TuffX getInstance() {
     return instance;
    }
}



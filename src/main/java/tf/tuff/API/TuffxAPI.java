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

    public TuffxAPI() {
    }

    public static ChunkSectionKey getChunkSectionKey(
            UUID player,
            String world,
            int x,
            int z,
            int y
    ) {
        return new ChunkSectionKey(player, world, x, z, y);
    }

    public static TuffxAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "The TuffX API is not initialized yet!"
            );
        }
        return instance;
    }

    public ServerRegistry getServerRegistry(JavaPlugin pl, String registryUrl, String serverAddr) {
        return new ServerRegistry(pl, registryUrl, serverAddr);
    }

    public static Map<String, Integer> getBlockIDMap() {
        return Collections.unmodifiableMap(BLOCK_ID_MAP);
    }

    public static void setInstance(TuffxAPI newInstance) {
        if (instance != null) {
            throw new IllegalStateException("TuffxAPI instance has already been set!");
        }
        instance = newInstance;
    }

    public static Integer getBlockName(int id) {
        return BLOCK_ID_MAP.get(id);
    }

    public static String getVersion() {
        return version;
    }

    public static int getBlockID(String name) {
        return BLOCK_ID_MAP.getOrDefault(name, 1);
    }
}



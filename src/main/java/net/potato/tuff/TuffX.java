package net.potato.tuff;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.logging.Level;

public class TuffX extends JavaPlugin implements Listener, PluginMessageListener {

    public static final String CHANNEL = "eagler:below_y0";
    public ViaBlockIds viablockids;

    private static final int CHUNKS_PER_TICK = 2;

    private final Map<UUID, Queue<Vector>> requestQueue = new ConcurrentHashMap<>();
    private final Map<UUID, Set<Vector>> currentlyQueued = new ConcurrentHashMap<>();
    
    private final Map<UUID, Boolean> initialLoadingPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> initialChunksToProcess = new ConcurrentHashMap<>();

    private BukkitTask processorTask;

    private void logDebug(String message) {
        getLogger().log(Level.INFO, "[TuffX-Debug] " + message);
    }

    @Override
    public void onEnable() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL, this);
        getServer().getPluginManager().registerEvents(this, this);
        if (this.viablockids == null) this.viablockids = new ViaBlockIds(this);
        startProcessorTask();
        logFancyEnable();
    }

    @Override
    public void onDisable() {
        if (processorTask != null) processorTask.cancel();
        requestQueue.clear();
        getLogger().info("TuffX has been disabled.");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(CHANNEL) || !player.isOnline()) return;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            int x = in.readInt();
            int y = in.readInt();
            int z = in.readInt();
            int actionLength = in.readUnsignedByte();
            byte[] actionBytes = new byte[actionLength];
            in.readFully(actionBytes);
            String action = new String(actionBytes, StandardCharsets.UTF_8);
            handleIncomingPacket(player, new Location(player.getWorld(), x, y, z), action, x, z);
        } catch (IOException e) {
            getLogger().warning("Failed to parse plugin message from " + player.getName() + ": " + e.getMessage());
        }
    }
    
    private void handleIncomingPacket(Player player, Location loc, String action, int chunkX, int chunkZ) {
        UUID playerId = player.getUniqueId();
        switch (action.toLowerCase()) {
            case "request_chunk":
                Vector chunkVec = new Vector(chunkX, 0, chunkZ);
                Set<Vector> queuedSet = currentlyQueued.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet());

                if (queuedSet.add(chunkVec)) { 
                    requestQueue.computeIfAbsent(playerId, k -> new ConcurrentLinkedQueue<>()).add(chunkVec);
                    
                    if (initialLoadingPlayers.getOrDefault(playerId, false)) {
                        initialChunksToProcess.merge(playerId, 1, Integer::sum);
                        logDebug("Player " + player.getName() + " needs chunk " + chunkX + "," + chunkZ + ". Total initial chunks to process: " + initialChunksToProcess.get(playerId));
                    }
                }
                break;
            case "ready":
                logDebug("Player " + player.getName() + " sent READY packet. Starting initial load sequence.");
                
                initialLoadingPlayers.put(playerId, true);
                initialChunksToProcess.put(playerId, 0);
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (initialLoadingPlayers.containsKey(playerId)) {
                            initialLoadingPlayers.put(playerId, false); // Lock the state.
                            logDebug("Player " + player.getName() + " initial chunk requests locked in at " + initialChunksToProcess.getOrDefault(playerId, 0) + " chunks.");
                            
                            if (initialChunksToProcess.getOrDefault(playerId, 0) == 0) {
                                checkIfInitialLoadComplete(player);
                            }
                        }
                    }
                }.runTaskLater(this, 5L);

                player.sendPluginMessage(this, CHANNEL, createBelowY0StatusPayload(true));
                break;
            case "use_on_block":
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Block block = loc.getBlock();
                        ItemStack item = player.getInventory().getItemInMainHand();
                        getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, item, block, block.getFace(player.getLocation().getBlock())));
                    }
                }.runTask(this);
                break;
        }
    }

    private byte[] createBelowY0StatusPayload(boolean status) {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(bout)) {
            out.writeUTF("belowy0_status");
            out.writeBoolean(status);
            return bout.toByteArray();
        } catch (IOException e) { return null; }
    }
    
    private void startProcessorTask() {
        this.processorTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID playerUUID : new HashSet<>(requestQueue.keySet())) {
                    Player player = getServer().getPlayer(playerUUID);
                    Queue<Vector> queue = requestQueue.get(playerUUID);

                    if (player == null || !player.isOnline()) {
                        requestQueue.remove(playerUUID);
                        initialLoadingPlayers.remove(playerUUID);
                        initialChunksToProcess.remove(playerUUID);
                        currentlyQueued.remove(playerUUID);
                        continue;
                    }
                    
                    if (queue == null || queue.isEmpty()) {
                        continue; 
                    }

                    for (int i = 0; i < CHUNKS_PER_TICK && !queue.isEmpty(); i++) {
                        Vector vec = queue.poll();
                        if (vec != null) {
                            World world = player.getWorld();
                            int cx = vec.getBlockX();
                            int cz = vec.getBlockZ();
                            if (world.isChunkLoaded(cx, cz)) {
                                processAndSendChunk(player, world.getChunkAt(cx, cz));
                            } else {
                                queue.add(vec); 
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    private void processAndSendChunk(final Player player, final Chunk chunk) {
        if (chunk == null || !player.isOnline()) return;

        final Vector chunkVec = new Vector(chunk.getX(), 0, chunk.getZ());
        logDebug("Processing chunk " + chunk.getX() + "," + chunk.getZ() + " for " + player.getName());

        new BukkitRunnable() {
            @Override
            public void run() {
                final ChunkSnapshot snapshot = chunk.getChunkSnapshot(true, false, false);
                final Map<BlockData, int[]> conversionCache = new HashMap<>();

                for (int sectionY = -4; sectionY < 0; sectionY++) {
                    if (!player.isOnline()) break;
                    try {
                        byte[] payload = createSectionPayload(snapshot, chunk.getX(), chunk.getZ(), sectionY, conversionCache);
                        if (payload != null) {
                            player.sendPluginMessage(TuffX.this, CHANNEL, payload);
                        }
                    } catch (IOException e) {
                        getLogger().severe("Payload creation failed for " + chunk.getX() + "," + chunk.getZ() + ": " + e.getMessage());
                    }
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline()) return;

                        Set<Vector> queuedSet = currentlyQueued.get(player.getUniqueId());
                        if (queuedSet != null) {
                            queuedSet.remove(chunkVec);
                        }
                        checkIfInitialLoadComplete(player);
                    }
                }.runTask(TuffX.this);
            }
        }.runTaskAsynchronously(this);
    }

    private void checkIfInitialLoadComplete(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (initialChunksToProcess.containsKey(playerId)) {
            int remaining = initialChunksToProcess.compute(playerId, (k, v) -> (v == null) ? -1 : v - 1);
            
            logDebug("Player " + player.getName() + " finished a chunk. Remaining initial chunks: " + remaining);
            
            if (remaining <= 0) {
                initialLoadingPlayers.remove(playerId); 
                initialChunksToProcess.remove(playerId);
                
                player.sendPluginMessage(this, CHANNEL, createLoadFinishedPayload());
                logDebug("INITIAL LOAD COMPLETE for " + player.getName() + ". Sent finished packet.");
            }
        }
    }

    private byte[] createLoadFinishedPayload() {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); 
            DataOutputStream out = new DataOutputStream(bout)) {
            
            out.writeUTF("y0_load_finished");
            
            return bout.toByteArray();
        } catch (IOException e) {
            getLogger().severe("Failed to create the y0_load_finished payload!");
            return null;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        requestQueue.remove(playerId);
        initialLoadingPlayers.remove(playerId);
        currentlyQueued.remove(playerId);
        initialChunksToProcess.remove(playerId);
    }
    
    private byte[] createWelcomePayload(String message, int someNumber) {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(bout)) {
            out.writeUTF("welcome_data");
            out.writeUTF(message);
            out.writeInt(someNumber);
            return bout.toByteArray();
        } catch (IOException e) { return null; }
    }
    
    private byte[] createSectionPayload(ChunkSnapshot snapshot, int cx, int cz, int sectionY, Map<BlockData, int[]> cache) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(8200); DataOutputStream out = new DataOutputStream(bout)) {
            out.writeUTF("chunk_data");
            out.writeInt(cx); out.writeInt(cz); out.writeInt(sectionY);
            boolean hasNonAirBlock = false;
            int baseY = sectionY * 16;
            for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                int worldY = baseY + y;
                BlockData blockData = snapshot.getBlockData(x, worldY, z);

                int[] legacyData = cache.computeIfAbsent(blockData, viablockids::toLegacy);

                if (legacyData[0] != 0) hasNonAirBlock = true;
                out.writeShort((short) ((legacyData[1] << 12) | (legacyData[0] & 0xFFF)));
            }
            return hasNonAirBlock ? bout.toByteArray() : null;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) { if (event.getBlock().getY() < 0) sendBlockUpdateToNearby(event.getBlock().getLocation(), Material.AIR.createBlockData()); }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) { if (event.getBlock().getY() < 0) sendBlockUpdateToNearby(event.getBlock().getLocation(), event.getBlock().getBlockData()); }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) { if (event.getBlock().getY() < 0) sendBlockUpdateToNearby(event.getBlock().getLocation(), event.getBlock().getBlockData()); }

    private void sendBlockUpdateToNearby(Location loc, BlockData data) {
        try {
            byte[] payload = createBlockUpdatePayload(loc, data);
            if (payload == null) return;
            for (Player p : loc.getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(loc) < 4096) p.sendPluginMessage(this, CHANNEL, payload);
            }
        } catch (IOException e) { getLogger().severe("Failed to send block update: " + e.getMessage()); }
    }
    
    private byte[] createBlockUpdatePayload(Location loc, BlockData data) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(bout)) {
            out.writeUTF("block_update");
            out.writeInt(loc.getBlockX()); out.writeInt(loc.getBlockY()); out.writeInt(loc.getBlockZ());
            int[] legacyData = viablockids.toLegacy(data);
            out.writeShort((short) ((legacyData[1] << 12) | (legacyData[0] & 0xFFF)));
            return bout.toByteArray();
        }
    }

    private void logFancyEnable() {
        getLogger().info("");
        getLogger().info("████████╗██╗   ██╗███████╗ ███████╗ ██╗  ██╗");
        getLogger().info("╚══██╔══╝██║   ██║██╔════╝ ██╔════╝ ╚██╗██╔╝");
        getLogger().info("   ██║   ██║   ██║██████╗  ██████╗   ╚███╔╝ ");
        getLogger().info("   ██║   ██║   ██║██╔═══╝  ██╔═══╝   ██╔██╗ ");
        getLogger().info("   ██║   ╚██████╔╝██║      ██║      ██╔╝╚██╗");
        getLogger().info("   ╚═╝    ╚═════╝ ╚═╝      ╚═╝      ╚═╝  ╚═╝");
        getLogger().info("");
        getLogger().info("Below y0 and TuffX programmed by Potato");
    }
}
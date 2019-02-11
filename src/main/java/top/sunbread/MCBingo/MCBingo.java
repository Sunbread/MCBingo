package top.sunbread.MCBingo;

import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import top.sunbread.MCBingo.commands.BingoCommandExecuter;
import top.sunbread.MCBingo.exceptions.PluginAlreadyLoadedException;
import top.sunbread.MCBingo.exceptions.PluginNotLoadedException;
import top.sunbread.MCBingo.game.BingoGameListener;
import top.sunbread.MCBingo.game.BingoGameStatus;
import top.sunbread.MCBingo.game.BingoPlayerManager;
import top.sunbread.MCBingo.game.ItemCheckRunnable;
import top.sunbread.MCBingo.gui.GUIListener;
import top.sunbread.MCBingo.lobby.BingoLobby;
import top.sunbread.MCBingo.lobby.BingoLobbyListener;
import top.sunbread.MCBingo.player.OriginStatusManager;
import top.sunbread.MCBingo.util.Utils;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MCBingo extends JavaPlugin {

    public static final String BINGO_GAME_OVERWORLD_NAME = "mcbingo_overworld";
    public static final String BINGO_GAME_NETHER_NAME = "mcbingo_nether";

    private BingoPlayerManager manager;
    private BingoLobby lobby;
    private boolean fatalError;
    private boolean loaded = false;

    @Override
    public void onEnable() {
        load(true);
        if (fatalError) {
            cleanupWorlds();
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("bingo").setExecutor(new BingoCommandExecuter(this));
        getCommand("bingo").setTabCompleter(null);
        getLogger().info("MCBingo v" + getDescription().getVersion() + " Enabled");
    }

    @Override
    public void onDisable() {
        if (loaded) unload(true);
        getCommand("bingo").setExecutor(null);
        getLogger().info("MCBingo v" + getDescription().getVersion() + " Disabled");
    }

    public BingoPlayerManager getPlayerManager() {
        if (!loaded) throw new PluginNotLoadedException();
        return manager;
    }

    public BingoLobby getLobby() {
        if (!loaded) throw new PluginNotLoadedException();
        return lobby;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean softReload() {
        if (loaded) unload(false);
        load(false);
        return loaded;
    }

    private void load(boolean hard) {
        if (loaded) throw new PluginAlreadyLoadedException();
        fatalError = false;
        if (hard)
            if (!initWorlds()) {
                fatalError = true;
                return;
            }
        saveDefaultConfig();
        reloadConfig();
        Location lobbyLocation = loadLobbyLocation();
        if (lobbyLocation == null) return;
        Set<Material> materials = loadMaterials();
        if (materials == null) return;
        register(materials, lobbyLocation);
        loaded = true;
    }

    private void unload(boolean hard) {
        if (!loaded) throw new PluginNotLoadedException();
        unregister();
        if (hard) cleanupWorlds();
        loaded = false;
    }

    private boolean initWorlds() {
        getLogger().info("Initializing game worlds...");
        cleanupWorlds();
        WorldCreator.name(BINGO_GAME_OVERWORLD_NAME).environment(Environment.NORMAL).createWorld().setKeepSpawnInMemory(false);
        WorldCreator.name(BINGO_GAME_NETHER_NAME).environment(Environment.NETHER).createWorld().setKeepSpawnInMemory(false);
        if (getServer().getWorld(BINGO_GAME_OVERWORLD_NAME) == null ||
                getServer().getWorld(BINGO_GAME_NETHER_NAME) == null) {
            getLogger().severe("Failed to enable: Unable to initialize game worlds");
            return false;
        }
        getLogger().info("World initialization complete!");
        return true;
    }

    private Location loadLobbyLocation() {
        getLogger().info("Loading lobby location...");
        if (!getConfig().contains("lobby-location.world") ||
                !getConfig().contains("lobby-location.x") ||
                !getConfig().contains("lobby-location.y") ||
                !getConfig().contains("lobby-location.z") ||
                !getConfig().contains("lobby-location.yaw") ||
                !getConfig().contains("lobby-location.pitch")) {
            getLogger().severe("Failed to load: Insufficient arguments of lobby location");
            return null;
        }
        World lobbyLocationWorld = getServer().getWorld(getConfig().getString("lobby-location.world"));
        if (lobbyLocationWorld == null ||
                lobbyLocationWorld.getName().equals(BINGO_GAME_OVERWORLD_NAME) ||
                lobbyLocationWorld.getName().equals(BINGO_GAME_NETHER_NAME)) {
            getLogger().severe("Failed to load: Invalid world of lobby location");
            return null;
        }
        double lobbyLocationX = getConfig().getDouble("lobby-location.x");
        double lobbyLocationY = getConfig().getDouble("lobby-location.y");
        double lobbyLocationZ = getConfig().getDouble("lobby-location.z");
        float lobbyLocationYaw = (float) getConfig().getDouble("lobby-location.yaw");
        float lobbyLocationPitch = (float) getConfig().getDouble("lobby-location.pitch");
        Location lobbyLocation = new Location(lobbyLocationWorld,
                lobbyLocationX,
                lobbyLocationY,
                lobbyLocationZ,
                lobbyLocationYaw,
                lobbyLocationPitch);
        getLogger().info("Loaded lobby location");
        return lobbyLocation;
    }

    private Set<Material> loadMaterials() {
        getLogger().info("Loading item material(s)...");
        List<String> materialNames = getConfig().getStringList("item-materials");
        Set<Material> materials = new HashSet<>();
        for (String materialName : materialNames) {
            Material material = Material.getMaterial(materialName);
            if (material == null) {
                getLogger().warning("Material " + materialName + " is not exist, skipping...");
                continue;
            }
            if (material == Material.AIR ||
                    material == Material.CAVE_AIR ||
                    material == Material.VOID_AIR) {
                getLogger().warning("Material " + materialName + " is air, skipping...");
                continue;
            }
            if (!material.isItem()) {
                getLogger().warning("Material " + materialName + " is not an obtainable item, skipping...");
                continue;
            }
            if (materials.contains(material)) {
                getLogger().warning("Material " + materialName + " is already loaded, skipping...");
                continue;
            }
            materials.add(material);
            getLogger().config("Loaded material " + materialName);
        }
        getLogger().info("Detected " + materialNames.size() + " item material(s) in config, and loaded " + materials.size() + " of them/it in fact.");
        if (materials.size() < BingoGameStatus.SIDE_LENGTH * BingoGameStatus.SIDE_LENGTH) {
            getLogger().severe("Failed to load: Insufficient loaded item material(s)");
            getLogger().severe("The plugin needs to load at least " +
                    BingoGameStatus.SIDE_LENGTH * BingoGameStatus.SIDE_LENGTH + " item material(s) in order to work properly.");
            return null;
        }
        return materials;
    }

    private void register(Set<Material> materials, Location lobbyLocation) {
        OriginStatusManager origin = new OriginStatusManager();
        manager = new BingoPlayerManager(materials, origin, this);
        lobby = new BingoLobby(manager, lobbyLocation, origin, this);
        new ItemCheckRunnable(manager, this).runTaskTimer(this, 0, 1);
        new BukkitRunnable() {
            @Override
            public void run() {
                manager.cleanup();
                lobby.cleanup();
            }
        }.runTaskTimer(this, 0, 600);
        getServer().getPluginManager().registerEvents(new BingoGameListener(manager, this), this);
        getServer().getPluginManager().registerEvents(new BingoLobbyListener(lobby), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
    }

    private void unregister() {
        manager.stopAllGames();
        lobby.kickAll();
        manager = null;
        lobby = null;
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
    }

    private void cleanupWorlds() {
        if (getServer().getWorld(BINGO_GAME_OVERWORLD_NAME) != null)
            getServer().unloadWorld(BINGO_GAME_OVERWORLD_NAME, true);
        if (getServer().getWorld(BINGO_GAME_NETHER_NAME) != null)
            getServer().unloadWorld(BINGO_GAME_NETHER_NAME, true);
        if (new File(getServer().getWorldContainer(), BINGO_GAME_OVERWORLD_NAME).exists())
            if (Utils.deleteFileOrDirectory(new File(getServer().getWorldContainer(), BINGO_GAME_OVERWORLD_NAME)))
                getLogger().config("Successfully deleted folder of the world " + BINGO_GAME_OVERWORLD_NAME);
            else
                getLogger().severe("Error: Cannot delete folder of the world " + BINGO_GAME_OVERWORLD_NAME);
        if (new File(getServer().getWorldContainer(), BINGO_GAME_NETHER_NAME).exists())
            if (Utils.deleteFileOrDirectory(new File(getServer().getWorldContainer(), BINGO_GAME_NETHER_NAME)))
                getLogger().config("Successfully deleted folder of the world " + BINGO_GAME_NETHER_NAME);
            else
                getLogger().severe("Error: Cannot delete folder of the world " + BINGO_GAME_NETHER_NAME);
    }

}

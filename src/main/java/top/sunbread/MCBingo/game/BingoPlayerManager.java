package top.sunbread.MCBingo.game;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import top.sunbread.MCBingo.MCBingo;
import top.sunbread.MCBingo.exceptions.AlreadyInGameException;
import top.sunbread.MCBingo.exceptions.NotInGameException;
import top.sunbread.MCBingo.gui.CardGUI;
import top.sunbread.MCBingo.player.OriginStatusManager;
import top.sunbread.MCBingo.util.Sounds;
import top.sunbread.MCBingo.util.Utils;
import top.sunbread.MCBingo.util.VariablePair;

import java.util.*;

public final class BingoPlayerManager {

    public static final int CENTER_X_FOR_GAMES = 0;
    public static final int CENTER_Z_FOR_GAMES = 0;
    public static final int SPREAD_RANGE_FOR_GAMES = 10000000;
    public static final int SPREAD_RANGE_FOR_PLAYERS = 500;
    public static final int MAX_TRY_TIMES = 10;

    private Map<UUID, BingoGameStatus> gameStatus = new HashMap<>();
    private Map<UUID, Location> spawnLocation = new HashMap<>();
    private Set<Material> materials;
    private OriginStatusManager origin;
    private JavaPlugin plugin;

    public BingoPlayerManager(Set<Material> materials, OriginStatusManager origin, JavaPlugin plugin) {
        this.materials = materials;
        this.origin = origin;
        this.plugin = plugin;
    }

    public synchronized Set<Material> getGameMaterials(Player player, boolean onlyUnmarked) {
        if (!gameStatus.containsKey(player.getUniqueId())) throw new NotInGameException();
        BingoGameStatus game = gameStatus.get(player.getUniqueId());
        Set<Material> gameMaterials = new HashSet<>();
        for (int row = 0; row < BingoGameStatus.SIDE_LENGTH; ++row)
            for (int col = 0; col < BingoGameStatus.SIDE_LENGTH; ++col)
                if (!onlyUnmarked || !game.getMark(player)[row][col])
                    gameMaterials.add(game.getCard()[row][col]);
        return gameMaterials;
    }

    public synchronized Location getGameSpawnLocation(Player player) {
        if (!gameStatus.containsKey(player.getUniqueId())) throw new NotInGameException();
        return spawnLocation.get(player.getUniqueId());
    }

    public synchronized boolean isInGame(Player player) {
        return gameStatus.containsKey(player.getUniqueId());
    }

    public synchronized void doMark(Material material, Player marker) {
        if (!gameStatus.containsKey(marker.getUniqueId())) throw new NotInGameException();
        BingoGameStatus game = gameStatus.get(marker.getUniqueId());
        if (game.doMark(material, marker)) {
            String materialName = Utils.getMaterialName(material);
            for (Player player : plugin.getServer().getOnlinePlayers())
                if (gameStatus.get(player.getUniqueId()) == game) {
                    CardGUI.refreshGUI(player);
                    if (!game.isBingo()) Sounds.playOnMarkItem(player);
                    player.sendMessage(Utils.getText("GAME_MARK_ITEM",
                            new VariablePair("player", marker.getName()),
                            new VariablePair("material", materialName)));
                    if (game.isBingo()) {
                        gameStatus.remove(player.getUniqueId());
                        exitGame(player);
                        Sounds.playOnWin(player);
                        player.sendMessage(Utils.getText("GAME_WIN",
                                new VariablePair("winner", marker.getName())));
                    }
                }
        }
    }

    public synchronized void leaveGame(Player player) {
        if (!gameStatus.containsKey(player.getUniqueId())) throw new NotInGameException();
        BingoGameStatus game = gameStatus.remove(player.getUniqueId());
        game.leaveGame(player);
        exitGame(player);
        player.sendMessage(Utils.getText("GAME_LEAVE"));
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers())
            if (gameStatus.get(onlinePlayer.getUniqueId()) == game)
                onlinePlayer.sendMessage(Utils.getText("GAME_SOMEONE_LEAVE",
                        new VariablePair("player", player.getName())));
    }

    public synchronized void showGUI(Player player) {
        if (!gameStatus.containsKey(player.getUniqueId())) throw new NotInGameException();
        CardGUI.showCard(gameStatus.get(player.getUniqueId()), player);
    }

    public synchronized void startGame(List<Player> players) {
        BingoGameStatus newGame = new BingoGameStatus(materials, players);
        Random rand = new Random();
        int targetX = rand.nextInt(SPREAD_RANGE_FOR_GAMES * 2 + 1) - SPREAD_RANGE_FOR_GAMES + CENTER_X_FOR_GAMES;
        int targetZ = rand.nextInt(SPREAD_RANGE_FOR_GAMES * 2 + 1) - SPREAD_RANGE_FOR_GAMES + CENTER_Z_FOR_GAMES;
        List<Location> finalLocations = calculateFinalLocations(players.size(), targetX, targetZ);
        for (int index = 0; index < players.size(); ++index) {
            if (gameStatus.containsKey(players.get(index).getUniqueId()))
                throw new AlreadyInGameException();
            gameStatus.put(players.get(index).getUniqueId(), newGame);
            enterGame(players.get(index), finalLocations.get(index));
            players.get(index).sendMessage(Utils.getText("GAME_START"));
            players.get(index).sendMessage(Utils.getText("GAME_GUI_HINT"));
        }
    }

    public synchronized void stopAllGames() {
        for (Player player : plugin.getServer().getOnlinePlayers())
            if (gameStatus.containsKey(player.getUniqueId())) {
                gameStatus.remove(player.getUniqueId());
                exitGame(player);
                player.sendMessage(Utils.getText("GAME_FORCE_STOP"));
            }
    }

    public synchronized void cleanup() {
        Set<UUID> gameStatusUUID = new HashSet<>(gameStatus.keySet());
        for (UUID uuid : gameStatusUUID)
            if (!plugin.getServer().getOnlinePlayers().contains(plugin.getServer().getPlayer(uuid))) {
                gameStatus.remove(uuid);
                spawnLocation.remove(uuid);
            }
        Set<BingoGameStatus> gameStatusSet = new HashSet<>(gameStatus.values());
        for (BingoGameStatus gameStatus : gameStatusSet) {
            Set<UUID> players = gameStatus.getPlayerUUIDs();
            for (UUID uuid : players) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (!plugin.getServer().getOnlinePlayers().contains(player))
                    gameStatus.leaveGame(player);
            }
        }
    }

    private List<Location> calculateFinalLocations(int amount, int targetX, int targetZ) {
        Random rand = new Random();
        World targetWorld = plugin.getServer().getWorld(MCBingo.BINGO_GAME_OVERWORLD_NAME);
        List<Location> finalLocations = new ArrayList<>();
        for (int times = 0; times < amount; ++times) {
            Block blockTarget = null;
            for (int tryTimes = 0; tryTimes < MAX_TRY_TIMES; ++tryTimes) {
                int blockTargetX = rand.nextInt(SPREAD_RANGE_FOR_PLAYERS * 2 + 1) - SPREAD_RANGE_FOR_PLAYERS + targetX;
                int blockTargetZ = rand.nextInt(SPREAD_RANGE_FOR_PLAYERS * 2 + 1) - SPREAD_RANGE_FOR_PLAYERS + targetZ;
                blockTarget = targetWorld.getHighestBlockAt(blockTargetX, blockTargetZ);
                if (blockTarget.getRelative(BlockFace.DOWN).getType().isSolid()) break;
            }
            Location finalLocation = blockTarget.getLocation();
            finalLocation.setX(finalLocation.getBlockX() + 0.5);
            finalLocation.setZ(finalLocation.getBlockZ() + 0.5);
            finalLocations.add(finalLocation);
        }
        return finalLocations;
    }

    private void enterGame(Player player, Location finalLocation) {
        spawnLocation.put(player.getUniqueId(), finalLocation);
        finalLocation.getBlock().setType(Material.AIR);
        finalLocation.getBlock().getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
        finalLocation.getBlock().getRelative(BlockFace.UP).setType(Material.AIR);
        CardGUI.closeGUI(player);
        player.setPlayerListName(ChatColor.GREEN + player.getName());
        player.getInventory().clear();
        player.teleport(finalLocation);
        player.setCompassTarget(finalLocation);
        player.setGameMode(GameMode.SURVIVAL);
        player.setFlying(false);
        player.setHealth(20);
        player.setLastDamage(0);
        player.setLastDamageCause(null);
        player.setNoDamageTicks(0);
        player.setTicksLived(1);
        player.setStatistic(Statistic.TIME_SINCE_REST, 0);
        player.setBedSpawnLocation(null);
        player.setFoodLevel(20);
        player.setSaturation(5);
        player.setExhaustion(0);
        player.setRemainingAir(300);
        player.setFireTicks(0);
        player.setFallDistance(0);
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());
        player.setExp(0);
        player.setLevel(0);
        showGUI(player);
        Sounds.playOnEnterGame(player);
    }

    private void exitGame(Player player) {
        spawnLocation.remove(player.getUniqueId());
        CardGUI.closeGUI(player);
        player.setPlayerListName(null);
        origin.restoreStatus(player);
        Sounds.playOnExitLobbyOrGameWithoutWinning(player);
    }

}

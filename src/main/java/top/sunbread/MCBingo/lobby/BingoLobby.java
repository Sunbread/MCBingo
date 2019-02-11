package top.sunbread.MCBingo.lobby;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import top.sunbread.MCBingo.exceptions.AlreadyInGameException;
import top.sunbread.MCBingo.exceptions.AlreadyInLobbyException;
import top.sunbread.MCBingo.exceptions.NotInLobbyException;
import top.sunbread.MCBingo.game.BingoPlayerManager;
import top.sunbread.MCBingo.player.OriginStatusManager;
import top.sunbread.MCBingo.timer.SyncCountdownTimer;
import top.sunbread.MCBingo.timer.TimerCallback;
import top.sunbread.MCBingo.util.Sounds;
import top.sunbread.MCBingo.util.Utils;
import top.sunbread.MCBingo.util.VariablePair;

import java.util.*;

public final class BingoLobby {

    public static final int COUNTDOWN_SECONDS = 45;

    private Set<UUID> players = new HashSet<>();
    private Set<UUID> readyPlayers = new HashSet<>();
    private SyncCountdownTimer startTimer;
    private BingoPlayerManager manager;
    private Location lobbyLocation;
    private OriginStatusManager origin;
    private JavaPlugin plugin;

    public BingoLobby(BingoPlayerManager manager, Location lobbyLocation, OriginStatusManager origin, JavaPlugin plugin) {
        TimerCallback startTimerCallback = new TimerCallback() {

            @Override
            public void onStart() {
                for (UUID uuid : players) {
                    plugin.getServer().getPlayer(uuid).setExp(1);
                    plugin.getServer().getPlayer(uuid).setLevel(COUNTDOWN_SECONDS);
                    Sounds.playOnTimerTick(plugin.getServer().getPlayer(uuid));
                    plugin.getServer().getPlayer(uuid).sendMessage(Utils.getText("LOBBY_ALL_READY"));
                    plugin.getServer().getPlayer(uuid).sendMessage(Utils.getText("LOBBY_TIMER_TICK",
                            new VariablePair("seconds", String.valueOf(COUNTDOWN_SECONDS))));
                }
            }

            @Override
            public void onTick(int remainingSeconds) {
                for (UUID uuid : players) {
                    plugin.getServer().getPlayer(uuid).setExp((float) remainingSeconds / (float) COUNTDOWN_SECONDS);
                    plugin.getServer().getPlayer(uuid).setLevel(remainingSeconds);
                    switch (remainingSeconds) {
                        case 30:
                        case 15:
                            Sounds.playOnTimerTick(plugin.getServer().getPlayer(uuid));
                            plugin.getServer().getPlayer(uuid).sendMessage(Utils.getText("LOBBY_TIMER_TICK",
                                    new VariablePair("seconds", String.valueOf(remainingSeconds))));
                            break;
                        case 10:
                        case 5:
                        case 4:
                        case 3:
                        case 2:
                        case 1:
                            Sounds.playOnTimerTickOnLastSeveralSeconds(plugin.getServer().getPlayer(uuid));
                            plugin.getServer().getPlayer(uuid).sendMessage(Utils.getText("LOBBY_TIMER_TICK_LAST_SECONDS",
                                    new VariablePair("seconds", String.valueOf(remainingSeconds))));
                            break;
                    }
                }
            }

            @Override
            public void onStop() {
                for (UUID uuid : players) {
                    plugin.getServer().getPlayer(uuid).setExp(0);
                    plugin.getServer().getPlayer(uuid).setLevel(0);
                    Sounds.playOnTimerStop(plugin.getServer().getPlayer(uuid));
                    plugin.getServer().getPlayer(uuid).sendMessage(Utils.getText("LOBBY_TIMER_STOP"));
                }
            }

            @Override
            public void onFinish() {
                for (UUID uuid : players) {
                    plugin.getServer().getPlayer(uuid).setExp(0);
                    plugin.getServer().getPlayer(uuid).setLevel(0);
                    Sounds.playOnTimerFinish(plugin.getServer().getPlayer(uuid));
                }
                List<Player> gamePlayers = new ArrayList<>();
                for (UUID uuid : readyPlayers) {
                    gamePlayers.add(plugin.getServer().getPlayer(uuid));
                    plugin.getServer().getPlayer(uuid).sendMessage(Utils.getText("LOBBY_GAME_START"));
                }
                players.clear();
                readyPlayers.clear();
                manager.startGame(gamePlayers);
            }

        };
        this.startTimer = new SyncCountdownTimer(COUNTDOWN_SECONDS, startTimerCallback, plugin);
        this.manager = manager;
        this.lobbyLocation = lobbyLocation;
        this.origin = origin;
        this.plugin = plugin;
    }

    public synchronized boolean isReady(Player player) {
        if (!players.contains(player.getUniqueId())) throw new NotInLobbyException();
        return readyPlayers.contains(player.getUniqueId());
    }

    public synchronized boolean isInLobby(Player player) {
        return players.contains(player.getUniqueId());
    }

    public synchronized void setReady(Player player, boolean ready) {
        if (!players.contains(player.getUniqueId())) throw new NotInLobbyException();
        if (ready && readyPlayers.contains(player.getUniqueId())) {
            player.sendMessage(Utils.getText("LOBBY_ALREADY_READY"));
            return;
        }
        if (!ready && !readyPlayers.contains(player.getUniqueId())) {
            player.sendMessage(Utils.getText("LOBBY_ALREADY_UNREADY"));
            return;
        }
        if (ready) {
            readyPlayers.add(player.getUniqueId());
            player.sendMessage(Utils.getText("LOBBY_READY"));
        } else {
            readyPlayers.remove(player.getUniqueId());
            player.sendMessage(Utils.getText("LOBBY_UNREADY"));
        }
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers())
            if (players.contains(onlinePlayer.getUniqueId()) &&
                    !onlinePlayer.getUniqueId().equals(player.getUniqueId()))
                if (ready)
                    onlinePlayer.sendMessage(Utils.getText("LOBBY_SOMEONE_READY",
                            new VariablePair("player", player.getName())));
                else
                    onlinePlayer.sendMessage(Utils.getText("LOBBY_SOMEONE_UNREADY",
                            new VariablePair("player", player.getName())));
        checkReady();
    }

    public synchronized void joinLobby(Player player) {
        if (manager.isInGame(player)) throw new AlreadyInGameException();
        if (players.contains(player.getUniqueId())) throw new AlreadyInLobbyException();
        players.add(player.getUniqueId());
        player.setPlayerListName(ChatColor.LIGHT_PURPLE + player.getName());
        origin.saveStatus(player);
        player.getInventory().clear();
        player.teleport(lobbyLocation);
        player.setGameMode(GameMode.ADVENTURE);
        player.setFlying(false);
        player.setHealth(20);
        player.setLastDamage(0);
        player.setLastDamageCause(null);
        player.setNoDamageTicks(0);
        player.setStatistic(Statistic.TIME_SINCE_REST, 0);
        player.setFoodLevel(20);
        player.setRemainingAir(300);
        player.setFireTicks(0);
        player.setFallDistance(0);
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());
        player.setExp(0);
        player.setLevel(0);
        Sounds.playOnEnterLobby(player);
        player.sendMessage(Utils.getText("LOBBY_JOIN"));
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers())
            if (players.contains(onlinePlayer.getUniqueId()) &&
                    !onlinePlayer.getUniqueId().equals(player.getUniqueId()))
                onlinePlayer.sendMessage(Utils.getText("LOBBY_SOMEONE_JOIN",
                        new VariablePair("player", player.getName())));
        checkReady();
    }

    public synchronized void leaveLobby(Player player) {
        if (!players.contains(player.getUniqueId())) throw new NotInLobbyException();
        players.remove(player.getUniqueId());
        readyPlayers.remove(player.getUniqueId());
        player.setPlayerListName(null);
        origin.restoreStatus(player);
        Sounds.playOnExitLobbyOrGameWithoutWinning(player);
        player.sendMessage(Utils.getText("LOBBY_LEAVE"));
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers())
            if (players.contains(onlinePlayer.getUniqueId()))
                onlinePlayer.sendMessage(Utils.getText("LOBBY_SOMEONE_LEAVE",
                        new VariablePair("player", player.getName())));
        checkReady();
    }

    public synchronized void kickAll() {
        for (Player player : plugin.getServer().getOnlinePlayers())
            if (players.contains(player.getUniqueId())) {
                players.remove(player.getUniqueId());
                readyPlayers.remove(player.getUniqueId());
                player.setPlayerListName(null);
                origin.restoreStatus(player);
                Sounds.playOnExitLobbyOrGameWithoutWinning(player);
                player.sendMessage(Utils.getText("LOBBY_KICKALL"));
            }
        startTimer.stop();
    }

    public synchronized void cleanup() {
        Set<UUID> playersUUID = new HashSet<>(players);
        Set<UUID> readyPlayersUUID = new HashSet<>(readyPlayers);
        for (UUID uuid : playersUUID)
            if (manager.isInGame(plugin.getServer().getPlayer(uuid)) ||
                    !plugin.getServer().getOnlinePlayers().contains(plugin.getServer().getPlayer(uuid)))
                players.remove(uuid);
        for (UUID uuid : readyPlayersUUID)
            if (!players.contains(uuid))
                readyPlayers.remove(uuid);
    }

    private void checkReady() {
        if (!players.isEmpty() && players.size() == readyPlayers.size())
            startTimer.start();
        else
            startTimer.stop();
    }

}

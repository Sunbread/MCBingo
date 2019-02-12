package top.sunbread.MCBingo.game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import top.sunbread.MCBingo.MCBingo;
import top.sunbread.MCBingo.util.Utils;

import java.lang.reflect.Method;

public final class BingoGameListener implements Listener {

    private BingoPlayerManager manager;
    private JavaPlugin plugin;

    public BingoGameListener(BingoPlayerManager manager, JavaPlugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (manager.isInGame(event.getPlayer()) &&
                event.getMessage().equals("#")) {
            event.setCancelled(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    manager.showGUI(event.getPlayer());
                }
            }.runTask(plugin);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (manager.isInGame(event.getPlayer()) &&
                !event.getMessage().toLowerCase().startsWith("/bingo")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Utils.getText("GAME_COMMAND_FORBIDDEN"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (manager.isInGame(event.getPlayer()) &&
                !event.getTo().getWorld().getName().equals(MCBingo.BINGO_GAME_OVERWORLD_NAME) &&
                !event.getTo().getWorld().getName().equals(MCBingo.BINGO_GAME_NETHER_NAME)) {
            event.setCancelled(true);
            if (event.getCause() != TeleportCause.UNKNOWN)
                event.getPlayer().sendMessage(Utils.getText("GAME_WORLD_EXITING_FORBIDDEN"));
        }
        if (!manager.isInGame(event.getPlayer()))
            if (event.getTo().getWorld().getName().equals(MCBingo.BINGO_GAME_OVERWORLD_NAME) ||
                    event.getTo().getWorld().getName().equals(MCBingo.BINGO_GAME_NETHER_NAME)) {
                event.setCancelled(true);
                if (event.getCause() != TeleportCause.UNKNOWN)
                    event.getPlayer().sendMessage(Utils.getText("GAME_WORLD_ENTERING_FORBIDDEN"));
            }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() == TeleportCause.NETHER_PORTAL)
            if (event.getFrom().getWorld().getName().equals(MCBingo.BINGO_GAME_OVERWORLD_NAME) ||
                    event.getFrom().getWorld().getName().equals(MCBingo.BINGO_GAME_NETHER_NAME)) {
                Location to = event.getFrom();
                if (event.getFrom().getWorld().getName().equals(MCBingo.BINGO_GAME_OVERWORLD_NAME))
                    to = new Location(plugin.getServer().getWorld(MCBingo.BINGO_GAME_NETHER_NAME),
                            Utils.clamp(-29999872, Math.floor(event.getFrom().getX() / 8), 29999872),
                            event.getFrom().getY(),
                            Utils.clamp(-29999872, Math.floor(event.getFrom().getZ() / 8), 29999872),
                            event.getFrom().getYaw(),
                            event.getFrom().getPitch());
                if (event.getFrom().getWorld().getName().equals(MCBingo.BINGO_GAME_NETHER_NAME))
                    to = new Location(plugin.getServer().getWorld(MCBingo.BINGO_GAME_OVERWORLD_NAME),
                            Utils.clamp(-29999872, Math.floor(event.getFrom().getX() * 8), 29999872),
                            event.getFrom().getY(),
                            Utils.clamp(-29999872, Math.floor(event.getFrom().getZ() * 8), 29999872),
                            event.getFrom().getYaw(),
                            event.getFrom().getPitch());
                event.setTo(to);
                event.useTravelAgent(true);
            }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (manager.isInGame(event.getPlayer()) &&
                event.getMaterial() == Material.ENDER_EYE &&
                event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                event.getClickedBlock().getType() == Material.END_PORTAL_FRAME) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Utils.getText("GAME_END_PORTAL_ACTIVATION_FORBIDDEN"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        if (manager.isInGame(event.getPlayer()) &&
                event.getBedEnterResult() == BedEnterResult.OK) {
            event.setCancelled(true);
            event.getPlayer().setStatistic(Statistic.TIME_SINCE_REST, 0);
            event.getPlayer().setBedSpawnLocation(event.getBed().getLocation());
            event.getPlayer().sendMessage(Utils.getText("GAME_BED_SPAWN_RECORD"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().isDead() && manager.isInGame(event.getEntity()))
            new BukkitRunnable() {
                @Override
                public void run() {
                    Server server = plugin.getServer();
                    Player player = event.getEntity();
                    try {
                        Object nmsMinecraftServer = server.getClass().getMethod("getServer").invoke(server);
                        Object nmsPlayerList = nmsMinecraftServer.getClass().getMethod("getPlayerList").invoke(nmsMinecraftServer);
                        Object nmsEntityPlayer = player.getClass().getMethod("getHandle").invoke(player);
                        Object nmsDimensionManager = Class.forName(nmsMinecraftServer.getClass().getPackage().getName() + ".DimensionManager").getField("OVERWORLD").get(null);
                        Method nmsMoveToWorld = nmsPlayerList.getClass().getMethod("moveToWorld", nmsEntityPlayer.getClass(), nmsDimensionManager.getClass(), boolean.class);
                        nmsMoveToWorld.invoke(nmsPlayerList, nmsEntityPlayer, nmsDimensionManager, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTask(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (manager.isInGame(event.getPlayer()) &&
                !event.isBedSpawn())
            event.setRespawnLocation(manager.getGameSpawnLocation(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (manager.isInGame(event.getPlayer()))
            manager.leaveGame(event.getPlayer());
    }

}

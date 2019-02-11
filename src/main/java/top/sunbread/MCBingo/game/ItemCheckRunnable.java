package top.sunbread.MCBingo.game;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class ItemCheckRunnable extends BukkitRunnable {

    private BingoPlayerManager manager;
    private JavaPlugin plugin;

    public ItemCheckRunnable(BingoPlayerManager manager, JavaPlugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers())
            if (manager.isInGame(player))
                for (Material material : manager.getGameMaterials(player, true)) {
                    if (player.getInventory().contains(material))
                        manager.doMark(material, player);
                    if (!manager.isInGame(player)) break;
                }
    }

}

package top.sunbread.MCBingo.player;

import org.bukkit.entity.Player;
import top.sunbread.MCBingo.exceptions.StatusAlreadyStoredException;
import top.sunbread.MCBingo.exceptions.StatusNotStoredException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class OriginStatusManager {

    private Map<UUID, PlayerOriginStatus> originStatus = new HashMap<>();

    public synchronized void saveStatus(Player player) {
        if (originStatus.containsKey(player.getUniqueId())) throw new StatusAlreadyStoredException();
        originStatus.put(player.getUniqueId(), new PlayerOriginStatus(player));
    }

    public synchronized void restoreStatus(Player player) {
        if (!originStatus.containsKey(player.getUniqueId())) throw new StatusNotStoredException();
        originStatus.remove(player.getUniqueId()).restore();
    }

}

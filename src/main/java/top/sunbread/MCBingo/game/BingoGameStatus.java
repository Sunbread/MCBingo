package top.sunbread.MCBingo.game;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import top.sunbread.MCBingo.exceptions.NoMaterialException;
import top.sunbread.MCBingo.exceptions.PlayerNotExistException;

import java.util.*;

public final class BingoGameStatus {

    public static final int SIDE_LENGTH = 5;

    private Material[][] card = new Material[SIDE_LENGTH][SIDE_LENGTH];
    private Map<UUID, BingoMark> marks = new HashMap<>();
    private boolean bingo;

    public BingoGameStatus(Set<Material> materialSet, List<Player> players) {
        if (materialSet.size() < SIDE_LENGTH * SIDE_LENGTH) throw new NoMaterialException();
        List<Material> shufflingMaterials = new ArrayList<>(materialSet);
        Collections.shuffle(shufflingMaterials);
        for (int rowIndex = 0; rowIndex < SIDE_LENGTH; ++rowIndex)
            for (int colIndex = 0; colIndex < SIDE_LENGTH; ++colIndex) {
                card[rowIndex][colIndex] = shufflingMaterials.get(rowIndex * SIDE_LENGTH + colIndex);
            }
        for (Player player : players)
            marks.put(player.getUniqueId(), new BingoMark());
        bingo = false;
    }

    public synchronized boolean doMark(Material material, Player marker) {
        if (!marks.containsKey(marker.getUniqueId())) throw new PlayerNotExistException();
        BingoMark mark = marks.get(marker.getUniqueId());
        for (int rowIndex = 0; rowIndex < SIDE_LENGTH; ++rowIndex)
            for (int colIndex = 0; colIndex < SIDE_LENGTH; ++colIndex)
                if (card[rowIndex][colIndex] == material)
                    if (mark.doMark(rowIndex, colIndex)) {
                        if (mark.isBingo()) bingo = true;
                        return true;
                    }
        return false;
    }

    public synchronized Material[][] getCard() {
        return card;
    }

    public synchronized boolean[][] getMark(Player player) {
        if (!marks.containsKey(player.getUniqueId())) throw new PlayerNotExistException();
        return marks.get(player.getUniqueId()).getMark();
    }

    public synchronized Set<UUID> getPlayerUUIDs() {
        return new HashSet<>(marks.keySet());
    }

    public synchronized boolean isBingo() {
        return bingo;
    }

    public synchronized void leaveGame(Player player) {
        if (!marks.containsKey(player.getUniqueId())) throw new PlayerNotExistException();
        marks.remove(player.getUniqueId());
    }

}

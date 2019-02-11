package top.sunbread.MCBingo.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import top.sunbread.MCBingo.game.BingoGameStatus;

public final class GUIInventoryHolder implements InventoryHolder {

    private BingoGameStatus gameStatus;
    private GUIType type;

    public GUIInventoryHolder(BingoGameStatus gameStatus, GUIType type) {
        this.gameStatus = gameStatus;
        this.type = type;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public BingoGameStatus getGameStatus() {
        return gameStatus;
    }

    public GUIType getGUIType() {
        return type;
    }

}

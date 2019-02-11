package top.sunbread.MCBingo.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import top.sunbread.MCBingo.game.BingoGameStatus;

public final class GUIListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player &&
                event.getInventory().getHolder() instanceof GUIInventoryHolder &&
                event.getSlotType() == SlotType.CONTAINER &&
                event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot()) {
            event.setCancelled(true);
            BingoGameStatus card = ((GUIInventoryHolder) event.getInventory().getHolder()).getGameStatus();
            if (card.isBingo()) {
                event.getWhoClicked().closeInventory();
                return;
            }
            if (event.getSlot() == 44)
                CardGUI.switchGUI((Player) event.getWhoClicked());
        }
    }

}

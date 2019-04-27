package top.sunbread.MCBingo.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.sunbread.MCBingo.game.BingoGameStatus;
import top.sunbread.MCBingo.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CardGUI {

    public static final int BUTTON_ITEM_SLOT = 44;

    public static void showCard(BingoGameStatus gameStatus, Player player) {
        Inventory inv = Bukkit.createInventory(new GUIInventoryHolder(gameStatus, GUIType.CARD_GUI), 45,
                Utils.getText("GUI_CARD_TITLE"));
        for (int row = 0; row < BingoGameStatus.SIDE_LENGTH; row++)
            for (int col = 0; col < BingoGameStatus.SIDE_LENGTH; col++) {
                int slot = row * 9 + 2 + col;
                Material currentMaterial = gameStatus.getCard()[row][col];
                ItemStack item = new ItemStack(currentMaterial);
                inv.setItem(slot, item);
            }
        ItemStack buttonItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = buttonItem.getItemMeta();
        meta.setDisplayName(Utils.getText("GUI_CARD_BUTTON"));
        buttonItem.setItemMeta(meta);
        inv.setItem(BUTTON_ITEM_SLOT, buttonItem);
        player.openInventory(inv);
    }

    public static void showMark(BingoGameStatus gameStatus, Player player) {
        Inventory inv = Bukkit.createInventory(new GUIInventoryHolder(gameStatus, GUIType.MARK_GUI), 45,
                Utils.getText("GUI_MARK_TITLE"));
        for (int row = 0; row < BingoGameStatus.SIDE_LENGTH; row++)
            for (int col = 0; col < BingoGameStatus.SIDE_LENGTH; col++) {
                int slot = row * 9 + 2 + col;
                ItemStack item;
                if (gameStatus.getMark(player)[row][col])
                    item = new ItemStack(Material.BARRIER);
                else
                    item = new ItemStack(Material.STRUCTURE_VOID);
                ItemMeta meta = item.getItemMeta();
                if (gameStatus.getMark(player)[row][col])
                    meta.setDisplayName(Utils.getText("GUI_MARK_MARKED"));
                else
                    meta.setDisplayName(Utils.getText("GUI_MARK_UNMARKED"));
                List<Player> otherMarkedPlayers = new ArrayList<>();
                for (UUID uuid : gameStatus.getPlayerUUIDs())
                    if (!uuid.equals(player.getUniqueId())) {
                        Player otherPlayer = Bukkit.getPlayer(uuid);
                        if (gameStatus.getMark(otherPlayer)[row][col])
                            otherMarkedPlayers.add(otherPlayer);
                    }
                if (!otherMarkedPlayers.isEmpty()) {
                    List<String> lore = new ArrayList<>();
                    lore.add(Utils.getText("GUI_MARK_OTHER_MARKED_PLAYERS"));
                    for (Player otherMarkedPlayer : otherMarkedPlayers)
                        lore.add(ChatColor.GREEN + otherMarkedPlayer.getName());
                    meta.setLore(lore);
                }
                item.setItemMeta(meta);
                inv.setItem(slot, item);
            }
        ItemStack buttonItem = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = buttonItem.getItemMeta();
        meta.setDisplayName(Utils.getText("GUI_MARK_BUTTON"));
        buttonItem.setItemMeta(meta);
        inv.setItem(BUTTON_ITEM_SLOT, buttonItem);
        player.openInventory(inv);
    }

    public static void refreshGUI(Player player) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (!(inv.getHolder() instanceof GUIInventoryHolder)) return;
        BingoGameStatus card = ((GUIInventoryHolder) inv.getHolder()).getGameStatus();
        if (card.isBingo()) {
            closeGUI(player);
            return;
        }
        if (((GUIInventoryHolder) inv.getHolder()).getGUIType() == GUIType.CARD_GUI) {
            showCard(card, player);
        }
        if (((GUIInventoryHolder) inv.getHolder()).getGUIType() == GUIType.MARK_GUI) {
            showMark(card, player);
        }
    }

    public static void switchGUI(Player player) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (!(inv.getHolder() instanceof GUIInventoryHolder)) return;
        BingoGameStatus card = ((GUIInventoryHolder) inv.getHolder()).getGameStatus();
        if (card.isBingo()) {
            closeGUI(player);
            return;
        }
        if (((GUIInventoryHolder) inv.getHolder()).getGUIType() == GUIType.CARD_GUI) {
            showMark(card, player);
        }
        if (((GUIInventoryHolder) inv.getHolder()).getGUIType() == GUIType.MARK_GUI) {
            showCard(card, player);
        }
    }

    public static void closeGUI(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof GUIInventoryHolder)
            player.closeInventory();
    }

}

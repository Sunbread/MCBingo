package top.sunbread.MCBingo.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class Sounds {

    public static void playOnEnterLobby(Player player) {
        player.playSound(player.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1f, 2f);
    }

    public static void playOnEnterGame(Player player) {
        player.playSound(player.getEyeLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1f, 1.5f);
    }

    public static void playOnExitLobbyOrGameWithoutWinning(Player player) {
        player.playSound(player.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1f, 2f);
    }

    public static void playOnTimerTick(Player player) {
        player.playSound(player.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1f);
    }

    public static void playOnTimerTickOnLastSeveralSeconds(Player player) {
        player.playSound(player.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);
    }

    public static void playOnTimerStop(Player player) {
        player.playSound(player.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 0.5f);
    }

    public static void playOnTimerFinish(Player player) {
        player.playSound(player.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 2f);
    }

    public static void playOnMarkItem(Player player) {
        player.playSound(player.getEyeLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
    }

    public static void playOnWin(Player player) {
        player.playSound(player.getEyeLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.5f);
    }

}

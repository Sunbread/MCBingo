package top.sunbread.MCBingo.timer;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class SyncCountdownTimer {

    private BukkitTask task;
    private int seconds;
    private TimerCallback callback;
    private JavaPlugin plugin;

    public SyncCountdownTimer(int seconds, TimerCallback callback, JavaPlugin plugin) {
        this.task = null;
        this.seconds = seconds;
        this.callback = callback;
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) return;
        task = new CountdownRunnable(seconds).runTaskTimer(plugin, 20, 20);
        callback.onStart();
    }

    public void stop() {
        if (task == null) return;
        task.cancel();
        task = null;
        callback.onStop();
    }

    private final class CountdownRunnable extends BukkitRunnable {

        private int remainingSeconds;

        public CountdownRunnable(int remainingSeconds) {
            this.remainingSeconds = remainingSeconds;
        }

        @Override
        public void run() {
            --remainingSeconds;
            if (remainingSeconds == 0) {
                cancel();
                task = null;
                callback.onFinish();
                return;
            }
            callback.onTick(remainingSeconds);
        }

    }

}

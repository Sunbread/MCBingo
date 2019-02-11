package top.sunbread.MCBingo.timer;

public interface TimerCallback {

    void onStart();

    void onTick(int remainingSeconds);

    void onStop();

    void onFinish();

}

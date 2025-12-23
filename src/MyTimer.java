public class MyTimer {
    private long startTime;
    private long pausedTime;
    private boolean isRunning;

    public void start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - pausedTime;
            isRunning = true;
        }
    }

    public void pause() {
        if (isRunning) {
            pausedTime = System.currentTimeMillis() - startTime;
            isRunning = false;
        }
    }

    public void reset() {
        startTime = 0;
        pausedTime = 0;
        isRunning = false;
    }

    public long getCurrentTime() {
        if (isRunning) {
            return System.currentTimeMillis() - startTime;
        } else {
            return pausedTime;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
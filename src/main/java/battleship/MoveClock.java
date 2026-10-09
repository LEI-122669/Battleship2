package battleship;

public class MoveClock {

    private long startTime;
    private long elapsedTime;
    private boolean running;

    public MoveClock() {
        this.startTime = 0;
        this.elapsedTime = 0;
        this.running = false;
    }

    public void start() {
        startTime = System.nanoTime();
        running = true;
    }

    public void stop() {
        if (!running) {
            return;
        }

        elapsedTime = System.nanoTime() - startTime;
        running = false;
    }

    public long getElapsedMilliseconds() {
        if (running) {
            return (System.nanoTime() - startTime) / 1_000_000;
        }

        return elapsedTime / 1_000_000;
    }

    public boolean isRunning() {
        return running;
    }

    public void reset() {
        startTime = 0;
        elapsedTime = 0;
        running = false;
    }
}

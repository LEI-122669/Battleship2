package battleship;

import org.apache.commons.lang3.time.StopWatch;

public class MoveTimer {

    private static final double MILLIS_PER_SECOND = 1000.0;
    private StopWatch stopWatch;

    public void start() {
        stopWatch = StopWatch.createStarted();
    }

    public long stop() {
        if (stopWatch == null || !stopWatch.isStarted()
                || stopWatch.isStopped()) {
            throw new IllegalStateException(
                    "No move is currently being timed.");
        }

        stopWatch.stop();
        return stopWatch.getTime();
    }

    public static String formatTime(long milliseconds) {
        double seconds = milliseconds / MILLIS_PER_SECOND;
        return String.format("%.2f seconds", seconds);
    }
}

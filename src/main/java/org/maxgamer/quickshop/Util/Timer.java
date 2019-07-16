package org.maxgamer.quickshop.Util;

import lombok.*;

@EqualsAndHashCode
@ToString
public class Timer {
    private long startTime;

    /**
     * Create a empty time, use setTimer to start
     */
    public Timer() {

    }

    /**
     * Create a empty time, auto start if autoSet is true
     * @param autoSet Auto set the timer
     */
    public Timer(boolean autoSet) {
        if (autoSet)
            startTime = System.currentTimeMillis();
    }

    /**
     * Create a empty time, use the param to init the startTime.
     *
     * @param startTime New startTime
     */
    public Timer(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Return how long time running when timer set and destory the timer.
     *
     * @return time
     */
    public long endTimer() {
        long time = System.currentTimeMillis() - startTime;
        startTime = 0;
        return time;
    }

    /**
     * Return how long time running after atTimeS. THIS NOT WILL DESTORY AND STOP THE TIMER
     *
     * @param atTime The inited time
     * @return time
     */
    public long getTimerAt(long atTime) {
        return atTime - startTime;
    }

    /**
     * Create a Timer.
     * Time Unit: ms
     */
    public void setTimer() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Return how long time running when timer set. THIS NOT WILL DESTORY AND STOP THE TIMER
     *
     * @return time
     */
    public long getTimer() {
        return System.currentTimeMillis() - startTime;
    }
}

package io.github.flbaue.marblemaze;

import android.graphics.Canvas;
import android.graphics.Color;

/**
 * Created by florian on 11.07.15.
 */
public class GameLoopThread extends Thread {
    static final long FPS = 30;
    private PlayView view;
    private boolean running = false;

    public GameLoopThread(PlayView view) {
        this.view = view;
    }

    public void setRunning(boolean run) {
        running = run;
    }

    @Override
    public void run() {
        long ticksPS = 1000 / FPS;
        long startTime;
        long sleepTime;
        while (running) {
            Canvas c = null;
            startTime = System.currentTimeMillis();
            try {
                c = view.getHolder().lockCanvas();
                if(!view.getHolder().getSurface().isValid())
                    continue;
                synchronized (view.getHolder()) {
                    view.onDraw(c);
                }
            } finally {
                if (c != null) {
                    view.getHolder().unlockCanvasAndPost(c);
                }
            }
            sleepTime = ticksPS-(System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 0)
                    sleep(sleepTime);
                else
                    sleep(10);
            } catch (Exception e) {}
        }
    }
}

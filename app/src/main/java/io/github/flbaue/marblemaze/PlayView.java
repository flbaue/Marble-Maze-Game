package io.github.flbaue.marblemaze;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.SensorEvent;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * TODO: document your custom view class.
 */
public class PlayView extends SurfaceView {

    private SurfaceHolder holder;
    private PlayLoopThread playLoopThread;

    private final Paint mLinesPaint = new Paint();
    private final Paint mPlayerPaint = new Paint();

    private int mWidth = 0;
    private int mHeight = 0;
    private float mTunnelLength;
    private float mTunnelHeight;

    private boolean mFirst = true;

    private Line[] mLines = new Line[4];
    private Bitmap mLinesBitmap;
    private Point mCenter;
    private Point mGoal;
    private Player mPlayer;

    private float mDeviceXAngle;
    private float mDeviceYAngle;
    private float mDeviceZAngle;
    private Paint mMovementPaint;

    private long mUpdateTime;

    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        playLoopThread = new PlayLoopThread(this);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                boolean retry = true;
                playLoopThread.setRunning(false);

                while (retry) {
                    try {
                        playLoopThread.join();
                        retry = false;
                    } catch (InterruptedException e) {

                    }
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                playLoopThread.setRunning(true);
                playLoopThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }
        });

        setup();
    }

    private void setup() {
        mUpdateTime = System.currentTimeMillis();

        mLinesPaint.setColor(Color.DKGRAY);
        mLinesPaint.setStyle(Paint.Style.STROKE);
        mLinesPaint.setStrokeWidth(dpToPx(2));
        mLinesPaint.setAntiAlias(true);

        mPlayerPaint.setColor(Color.RED);
        mPlayerPaint.setStyle(Paint.Style.FILL);
        mPlayerPaint.setAntiAlias(true);

        mMovementPaint = new Paint();
        mMovementPaint.setColor(Color.BLACK);
        mMovementPaint.setAntiAlias(true);
        mMovementPaint.setTextSize(dpToPx(10));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvas == null) {
            return;
        }

        if (mFirst) {
            mWidth = getWidth();
            mHeight = getHeight();
            mCenter = new Point(mWidth / 2, mHeight / 2);
        }

        canvas.drawColor(Color.WHITE);
        drawLines(canvas);
        drawPlayer(canvas);
        updatePlayer();
        if (playerCollision()) {
            canvas.drawColor(Color.parseColor("#77FF0000"));
        } else if (goalReached()) {
            canvas.drawColor(Color.parseColor("#7700FFAA"));
        }


        //drawDebugInfo(canvas);

        if (mFirst) {
            mFirst = false;
        }
    }

    private boolean goalReached() {
        float a = (float) Math.pow(mPlayer.x - mGoal.x, 2);
        float b = (float) Math.pow(mPlayer.y - mGoal.y, 2);
        float c2 = a + b;

        if (c2 <= Math.pow(mPlayer.r, 2)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean playerCollision() {
        for (Line line : mLines) {
            if (playerIntersection(line)) {
                return true;
            }
        }
        return false;
    }

    private boolean playerIntersection(Line l) {
        float x0 = mPlayer.x;
        float y0 = mPlayer.y;
        float x1 = l.x1;
        float y1 = l.y1;
        float x2 = l.x2;
        float y2 = l.y2;
        float n = Math.abs((x2 - x1) * (y1 - y0) - (x1 - x0) * (y2 - y1));
        double d = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        double dist = n / d;
        if (dist > mPlayer.r) return false;
        double d1 = Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));
        if ((d1 - mPlayer.r) > d) return false;
        double d2 = Math.sqrt((x0 - x2) * (x0 - x2) + (y0 - y2) * (y0 - y2));
        if ((d2 - mPlayer.r) > d) return false;
        return true;
    }

    private void drawDebugInfo(Canvas canvas) {
        String movX = "X: " + mDeviceXAngle;
        String movY = "Y: " + mDeviceYAngle;
        String movZ = "Z: " + mDeviceZAngle;

        canvas.drawText(movX, 0, dpToPx(12), mMovementPaint);
        canvas.drawText(movY, 0, dpToPx(24), mMovementPaint);
        canvas.drawText(movZ, 0, dpToPx(32), mMovementPaint);
    }

    private void drawPlayer(Canvas canvas) {
        if (mFirst) {
            float r = mTunnelHeight * 0.4f;
            mPlayer = new Player(r, mCenter.x - (mTunnelLength / 2) + mTunnelHeight / 2, mCenter.y, 0, 0);
        }

        canvas.drawCircle(mPlayer.x, mPlayer.y, mPlayer.r, mPlayerPaint);
    }

    private void updatePlayer() {

        int xSpeedF = Math.round(-mDeviceYAngle);
        int ySpeedF = Math.round(mDeviceZAngle);

        mPlayer.sX += 0.1f * xSpeedF;
        mPlayer.sY += 0.1f * ySpeedF;

        if (mPlayer.sX != 0) {
            mPlayer.sX = mPlayer.sX - mPlayer.sX * 0.1f;
        }

        if (mPlayer.sY != 0) {
            mPlayer.sY = mPlayer.sY - mPlayer.sY * 0.1f;
        }

        mPlayer.x += mPlayer.sX;
        mPlayer.y += mPlayer.sY;
    }

    private void drawLines(Canvas canvas) {
        if (mFirst) {
            mTunnelLength = mWidth * 0.8f;
            mTunnelHeight = mHeight * 0.15f;
            float tHeightOffset = mTunnelHeight / 2;
            float tLengthOffset = mTunnelLength / 2;

            mLines[0] = new Line(mCenter.x - tLengthOffset, mCenter.y - tHeightOffset, mCenter.x + tLengthOffset, mCenter.y - tHeightOffset);
            mLines[1] = new Line(mCenter.x - tLengthOffset, mCenter.y + tHeightOffset, mCenter.x + tLengthOffset, mCenter.y + tHeightOffset);
            mLines[2] = new Line(mCenter.x - tLengthOffset, mCenter.y + tHeightOffset, mCenter.x - tLengthOffset, mCenter.y - tHeightOffset);
            mLines[3] = new Line(mCenter.x + tLengthOffset, mCenter.y - tHeightOffset, mCenter.x + tLengthOffset, mCenter.y + tHeightOffset);

            mLinesBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_4444);
            Canvas linesCanvas = new Canvas(mLinesBitmap);
            for (Line line : mLines) {
                linesCanvas.drawLine(line.x1, line.y1, line.x2, line.y2, mLinesPaint);
            }

            mGoal = new Point(Math.round(mCenter.x + tLengthOffset - mTunnelHeight * 0.4f), mCenter.y);
            linesCanvas.drawCircle(mGoal.x, mGoal.y, dpToPx(4), mPlayerPaint);
        }

        canvas.drawBitmap(mLinesBitmap, 0, 0, null);
    }

    private float dpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void updateMovement(SensorEvent event) {

        mDeviceXAngle = event.values[0];
        mDeviceYAngle = event.values[1];
        mDeviceZAngle = event.values[2];
    }

    private class Player {
        public final float r;
        public float x;
        public float y;
        public float sX;
        public float sY;

        public Player(float r, float x, float y, float sX, float sY) {
            this.r = r;
            this.x = x;
            this.y = y;
            this.sX = sX;
            this.sY = sY;
        }
    }

    private class Line {
        public final float x1;
        public final float y1;
        public final float x2;
        public final float y2;

        public Line(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }
}

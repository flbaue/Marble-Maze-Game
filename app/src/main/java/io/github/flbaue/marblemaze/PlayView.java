package io.github.flbaue.marblemaze;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.SensorEvent;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * TODO: document your custom view class.
 */
public class PlayView extends SurfaceView {

    private SurfaceHolder holder;
    private GameLoopThread gameLoopThread;

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
    private Player mPlayer;

    private float mMovement_x_angle;
    private float mMovement_y_angle;
    private float mMovement_z_angle;
    private Paint mMovementpaint;

    private long mUpdateTime;

    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gameLoopThread = new GameLoopThread(this);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                gameLoopThread.setRunning(false);
                while (retry) {
                    try {
                        gameLoopThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                gameLoopThread.setRunning(true);
                gameLoopThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
            }
        });

        init();
    }

    private void init() {
        mUpdateTime = System.currentTimeMillis();

        mLinesPaint.setColor(Color.DKGRAY);
        mLinesPaint.setStyle(Paint.Style.STROKE);
        mLinesPaint.setStrokeWidth(dpToPx(2));
        mLinesPaint.setAntiAlias(true);

        mPlayerPaint.setColor(Color.RED);
        mPlayerPaint.setStyle(Paint.Style.FILL);
        mPlayerPaint.setAntiAlias(true);

        mMovementpaint = new Paint();
        mMovementpaint.setColor(Color.BLACK);
        mMovementpaint.setAntiAlias(true);
        mMovementpaint.setTextSize(dpToPx(10));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mFirst) {
            mWidth = getWidth();
            mHeight = getHeight();
            mCenter = new Point(mWidth / 2, mHeight / 2);
        }

        canvas.drawColor(Color.WHITE);

        drawLines(canvas);
        drawPlayer(canvas);
        drawMovement(canvas);

        if (mFirst) {
            mFirst = false;
        }
    }

    private void drawMovement(Canvas canvas) {
        String movX = "X: " + mMovement_x_angle;
        String movY = "Y: " + mMovement_y_angle;
        String movZ = "Z: " + mMovement_z_angle;

        canvas.drawText(movX, 0, dpToPx(12), mMovementpaint);
        canvas.drawText(movY, 0, dpToPx(24), mMovementpaint);
        canvas.drawText(movZ, 0, dpToPx(32), mMovementpaint);
    }

    private void drawPlayer(Canvas canvas) {
        if (mFirst) {
            float r = mTunnelHeight * 0.4f;
            mPlayer = new Player(r, mCenter.x - (mTunnelLength / 2) + mTunnelHeight / 2, mCenter.y, 0, 0);
        }
        updatePlayer();
        canvas.drawCircle(mPlayer.x, mPlayer.y, mPlayer.r, mPlayerPaint);
    }

    private void updatePlayer() {

        int xSpeedF = Math.round(-mMovement_y_angle);
        int ySpeedF = Math.round(mMovement_z_angle);

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
        }

        canvas.drawBitmap(mLinesBitmap, 0, 0, null);
    }

    private float dpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void updateMovement(SensorEvent event) {

        mMovement_x_angle = event.values[0];
        mMovement_y_angle = event.values[1];
        mMovement_z_angle = event.values[2];
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

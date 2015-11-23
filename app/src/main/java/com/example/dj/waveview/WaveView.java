package com.example.dj.waveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author DragonJiang
 * @date 2015/11/23
 * @Description:
 */
public class WaveView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;

    private LocalThread mThread;

    private Context mContext;
    private int mWidth;
    private int mHeight;
    private static final int DEFAULT_WAVE_NUM = 10;


    public WaveView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
//        this.setZOrderOnTop(true);
//        mHolder.setFormat(PixelFormat.TRANSPARENT);//设置为透明
        mThread = new LocalThread(mHolder);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread.start();
        mThread.startWave();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mThread.stopWave();
        mThread.interrupt();
    }

    /**
     * set wave range
     *
     * @param scale
     */
    public void setScale(float scale) {
        mThread.setScale(scale);
    }

    public void stopWave() {
        mThread.stopWave();
    }

    public void startWave() {
        mThread.startWave();
    }


    class LocalThread extends Thread {
        private SurfaceHolder holder;
        public boolean isRun = true;
        float speed = 0;
        float scale = 0;
        float lastScale = 0;
        Paint[] paints = new Paint[DEFAULT_WAVE_NUM];
        Path[] paths = new Path[DEFAULT_WAVE_NUM];

        public LocalThread(SurfaceHolder holder) {
            this.holder = holder;

            int dif = (255 - 25) / paints.length;
            for (int i = 0; i < paints.length; i++) {
                paints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
                paints[i].setStyle(Paint.Style.STROKE);
                paints[i].setStrokeWidth(1.0f);
                paints[i].setColor(mContext.getResources().getColor(R.color.colorPrimary));
                paints[i].setTextSize(40);
                paints[i].setAlpha(255 - i * dif);
            }
        }

        public void setScale(float scale) {
            this.scale = (lastScale - scale)/2 + scale;
        }

        public void startWave() {
            scale = 0;
            for(int i=0; i<paths.length; i++){
                if(paths[i] != null){
                    paths[i].reset();
                }
            }
            isRun = true;
        }

        public void stopWave() {
            isRun = false;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (this) {
                        if (isRun) {
                            preDraw();
                            Canvas canvas = holder.lockCanvas();    //锁定画布
                            draw(canvas);
                            holder.unlockCanvasAndPost(canvas);     //结束锁定画图，并提交改变。
                            afterDraw();
                        } else {
                            Canvas canvas = holder.lockCanvas();    //锁定画布
                            canvas.drawColor(Color.WHITE);          //设置画布背景颜色
                            canvas.drawLine(0, mHeight / 2, mWidth, mHeight / 2, paints[0]);
                            holder.unlockCanvasAndPost(canvas);     //结束锁定画图，并提交改变。
                        }
                    }
                    Thread.sleep(1000 / 60);                        //60帧
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 计算主正弦，设置scale，设置speed
         */
        private void preDraw() {
            if (speed < mWidth-0.15) {
                speed += 0.15;
            } else {
                speed = 0;
            }
            if (scale > 1) {
                scale = 1;
            }
            float cx = 0;
            float cy = mHeight / 2;
            float step = 2;
            float coefficient = mHeight / 4;//系数
            float midW = mWidth / 2;

            paints[0].setStrokeWidth(3.0f);
            paths[0] = new Path();
            paths[0].moveTo(cx, cy);
            //画主正弦
            for (int i = 0; i < mWidth; i += step) {
                float x = i;
                float y = (float) (scale
                        * coefficient
                        * (1 - Math.pow(1 - x / midW, 2))
                        * Math.sin(Math.PI * 2 * x / mWidth + speed))
                        + mHeight / 2;
                paths[0].lineTo(x, y);
            }
        }

        /**
         * 绘制波形
         *
         * @param canvas
         */
        private void draw(Canvas canvas) {
            if (canvas == null) {
                return;
            }

            //init canvas
            canvas.drawColor(Color.WHITE);   //设置画布背景颜色

            //画正弦，其中第一条为主正弦
            for (int j = 0; j < paths.length; j++) {
                if (paths[j] != null) {
                    canvas.drawPath(paths[j], paints[j]);
                }
            }
        }

        /**
         * 波形迭代
         */
        private void afterDraw() {
            float offset = mHeight / 2 * 0.2f;
            for (int j = paths.length - 1; j > 0; j--) {
                paths[j] = paths[j - 1];
                Matrix m = new Matrix();
                m.setScale(1, 0.8f);
                if (paths[j] != null) {
                    paths[j].transform(m);//x 坐标缩放为0.8， 所以offset要下移0.2的mHeight / 2
                    paths[j].offset(0, offset);
                }
            }
        }
    }
}

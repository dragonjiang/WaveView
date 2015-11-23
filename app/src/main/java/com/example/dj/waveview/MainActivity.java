package com.example.dj.waveview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView mTextView;
    private WaveView mWaveView;
    private Button mBtnStart;
    private Button mBtnStop;
    private Timer mTimer;
    private float mPreValue;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    mTextView.setText((CharSequence) msg.obj);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text_view);
        mWaveView = (WaveView) findViewById(R.id.wave_view);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mBtnStart.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);

        mPreValue = 32767/3;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                float value = calcNext();
                String s = "CurValue = " + value;
                Message msg = new Message();
                msg.what = 0;
                msg.obj = s;
                mHandler.sendMessage(msg);
                mWaveView.setScale(value);
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 20);
    }

    private float calcNext(){
        float value;
        value = random() + mPreValue;
        if(value > 32767){
            value = 32767;
        }
        if(value < 0){
            value = 0;
        }

        mPreValue = value;
        return value / 10000;
    }

    private float random(){
        return randomCommon(0, 100);
    }

    /**
     * 随机指定范围内N个不重复的数
     * 最简单最基本的方法
     * 取 -max<x<-min || min < x < max 的环形区域
     *
     * @param min 指定范围最小值
     * @param max 指定范围最大值
     */
    public static float randomCommon(float min, float max) {
        if (max < min) {
            return 0;
        }

        float result = 0;
        boolean flag = true;
        while (flag) {
            float num = (float) Math.random() * max * 2 - max;

            if (-min < num && num < min) {
                    flag = true;
            } else {
                flag = false;
            }

            result = num;
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_start){
            mWaveView.startWave();
        } else if(v.getId() == R.id.btn_stop){
            mWaveView.stopWave();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }
}

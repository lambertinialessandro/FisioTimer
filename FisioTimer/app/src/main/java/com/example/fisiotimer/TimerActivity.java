package com.example.fisiotimer;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class TimerActivity extends AppCompatActivity {
    private Handler handler;
    private Timer timer;
    private TimerTask timerTask;
    private PowerManager.WakeLock wakeLock;

    private ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    private boolean volume;
    private boolean vibrate;

    private RelativeLayout relLayTimer;
    private TextView txtPhase;
    private TextView txtPauseTime;
    private TextView txtExerciseTime;
    private ProgressBar progressBar;
    private TextView txtRepetitions;
    private CheckBox checkBoxVolume;
    private CheckBox checkBoxVibrate;
    private CheckBox checkBoxDoInBackground;
    private Button btnStopTimer;
    private Button btnResumeTimer;
    private Button btnRestartTimer;

    private boolean isWorking = false;
    private boolean shouldRun = true;
    private boolean runInBackground = true;
    private long timeBetween;

    private int proggressState = 0;

    private long startTime;
    private long nowTime;
    private long totTime;

    private int dim;
    private int pos;
    private long[] intervals;
    private int[] colors;
    private int repetitions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        PowerManager powerManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(FLAG_KEEP_SCREEN_ON, "myLock:myLock");
        startWakeLock();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        relLayTimer = (RelativeLayout) findViewById(R.id.relLayTimer);
        txtPhase = (TextView) findViewById(R.id.txtPhase);
        txtPauseTime = (TextView) findViewById(R.id.txtPauseTime);
        txtExerciseTime = (TextView) findViewById(R.id.txtExerciseTime);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtRepetitions = (TextView) findViewById(R.id.txtRepetitions);
        checkBoxVolume = (CheckBox) findViewById(R.id.checkBoxVolume);
        checkBoxVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox v = (CheckBox) view;
                volume = v.isChecked();
            }
        });
        checkBoxVibrate = (CheckBox) findViewById(R.id.checkBoxVibrate);
        checkBoxVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox v = (CheckBox) view;
                vibrate = v.isChecked();
            }
        });
        checkBoxDoInBackground = (CheckBox) findViewById(R.id.checkBoxDoInBackground);
        checkBoxDoInBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox v = (CheckBox) view;
                runInBackground = v.isChecked();
            }
        });

        volume = checkBoxVolume.isChecked();
        vibrate = checkBoxVibrate.isChecked();
        runInBackground = checkBoxDoInBackground.isChecked();

        btnStopTimer = (Button) findViewById(R.id.btnStopTimer);
        btnStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldRun = false;
                btnStopTimer.setEnabled(false);
                btnResumeTimer.setEnabled(true);
                timeBetween = System.currentTimeMillis()/1000;
            }
        });
        btnResumeTimer = (Button) findViewById(R.id.btnResumeTimer);
        btnResumeTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldRun = true;
                btnStopTimer.setEnabled(true);
                btnResumeTimer.setEnabled(false);
                timeBetween = ((System.currentTimeMillis()/1000) - timeBetween);
                startTime = startTime + timeBetween;
                nowTime = nowTime + timeBetween;
            }
        });
        btnRestartTimer = (Button) findViewById(R.id.btnRestartTimer);
        btnRestartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldRun = true;
                btnStopTimer.setEnabled(true);
                btnResumeTimer.setEnabled(false);
                stopTimer();

                startTime = System.currentTimeMillis() / 1000;
                txtExerciseTime.setText("0 S");
                if (repetitions % 2 != 0){
                    repetitions--;
                }
                pos = repetitions % dim;
                proggressState = 0;
                txtPhase.setText("Exercise");
                txtPauseTime.setText("Total time: " + (int) (intervals[pos]) + " S");
                txtRepetitions.setText("Repetitions: " + (int) repetitions / 2);
                relLayTimer.setBackgroundColor(colors[pos]);

                startTimer();
            }
        });


        dim = 2;
        repetitions = 0;
        intervals = new long[dim];
        intervals[0] = getIntent().getExtras().getInt("pauseTime");
        intervals[1] = getIntent().getExtras().getInt("exerciseTime");
        colors = new int[dim];
        colors[0] = Color.parseColor("#2B2B2B");
        colors[1] = Color.parseColor("#D81B60");

        pos = repetitions%dim;
        relLayTimer.setBackgroundColor(colors[pos]);
        progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#C1C1C1"), android.graphics.PorterDuff.Mode.SRC_IN);
        txtPhase.setText("Exercise");
        txtPauseTime.setText("Total time: " + (int)(intervals[pos]) +" S");
        txtRepetitions.setText("Repetitions: "+ (int)repetitions/2);

        startTime = System.currentTimeMillis()/1000;

        handler = new Handler();
        timer = new Timer();

        startTimer();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if(!runInBackground)
            stopTimer();

        timer.cancel();
        stopWakeLock();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!runInBackground)
            startTimer();

        startWakeLock();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(!runInBackground)
            stopTimer();

        stopWakeLock();
    }

    private synchronized void startWakeLock(){
        if(!wakeLock.isHeld())
            wakeLock.acquire();
    }

    private synchronized void stopWakeLock(){
        if(wakeLock.isHeld())
            wakeLock.release();
    }

    private synchronized void startTimer(){
        if(!isWorking) {
            isWorking = true;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (shouldRun) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                nowTime = System.currentTimeMillis() / 1000;

                                totTime = nowTime - startTime;
                                txtExerciseTime.setText((int) (totTime) + " S");

                                proggressState = (int) (totTime * 100 / intervals[pos]);

                                if (nowTime >= startTime + intervals[pos]) {
                                    if (volume) {
                                        toneGen.startTone(ToneGenerator.TONE_CDMA_LOW_SSL, 200);
                                    }
                                    if (vibrate) {
                                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                                        } else {
                                            //deprecated in API 26
                                            v.vibrate(150);
                                        }
                                    }
                                    startTime = System.currentTimeMillis() / 1000;
                                    totTime = nowTime - startTime;
                                    txtExerciseTime.setText("0 S");
                                    repetitions++;
                                    pos = repetitions % dim;
                                    proggressState = (int) (totTime * 100 / intervals[pos]);
                                    if(repetitions % 2 == 0){
                                        txtPhase.setText("Exercise");
                                    } else {
                                        txtPhase.setText("Pause");
                                    }
                                    txtPauseTime.setText("Total time: " + (int) (intervals[pos]) + " S");
                                    txtRepetitions.setText("Repetitions: " + (int) repetitions / 2);
                                    relLayTimer.setBackgroundColor(colors[pos]);
                                } else if (nowTime >= startTime + intervals[pos] - 2
                                        || nowTime == startTime + intervals[pos] - 5) {
                                    if (volume) {
                                        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                                    }
                                }
                            }
                        });

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(proggressState);
                            }
                        });
                    }
                }
            }, 0, 1000);
        }
    }

    private synchronized void stopTimer(){
        if(isWorking) {
            isWorking = false;
            timer.cancel();
        }
    }
}

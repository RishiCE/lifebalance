package project.first.lifebalance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aar.tapholdupbutton.TapHoldUpButton;
import com.github.onlynight.waveview.WaveView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class CheckUpActivity extends AppCompatActivity {
    private Timer timer;
    private int counter = 4;
    private WaveView wave;
    private final MediaPlayer player = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkup);

        TapHoldUpButton trigger = findViewById(R.id.trigger);
        TextView counter = findViewById(R.id.counter);
        wave = findViewById(R.id.wave);

        player.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .build());
        try {
            player.setDataSource(getResources().openRawResourceFd(R.raw.high2low));
            player.prepareAsync();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Toast.makeText(CheckUpActivity.this, "Test Completed", Toast.LENGTH_SHORT).show();
                    stopTest();
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "Something horribly went wrong. PS : IOException", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }

        trigger.setOnButtonClickListener(new TapHoldUpButton.OnButtonClickListener() {
            @Override
            public void onLongHoldStart(View v) {
                timer = new Timer();
                counter.setVisibility(View.VISIBLE);
                timer.scheduleAtFixedRate(new TimerTask(){
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                            if (CheckUpActivity.this.counter == 0){
                                counter.setVisibility(View.GONE);
                                startTest();
                                cancel();
                            } else {
                                CheckUpActivity.this.counter--;
                                counter.setText(String.valueOf(CheckUpActivity.this.counter));
                            }
                        });
                    }
                }, 0, 1000);
            }

            @Override
            public void onLongHoldEnd(View v) {
                if (CheckUpActivity.this.counter == 0)
                    stopTest();
                else {
                    counter.setVisibility(View.GONE);
                    timer.cancel();
                    CheckUpActivity.this.counter = 4;
                    Toast.makeText(CheckUpActivity.this, "Please keep pressing the button to start the test", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onClick(View v) {
                Toast.makeText(CheckUpActivity.this, "Please keep pressing the button to start the test", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopTest() {
        if (player.isPlaying()){
            Toast.makeText(this, "Calculating result....", Toast.LENGTH_SHORT).show();
            wave.stop();
            var timePlayed = player.getCurrentPosition()/1000;
            player.stop();
            showResult(timePlayed);
        }
    }

    private void startTest() {
        wave.start();
        player.start();
    }

    private void showResult(int timePlayed) {
        TestResultActivity.Grade result;
        int frequency = 0;
        if (timePlayed < 5) {
            Toast.makeText(this, "Test did not completed", Toast.LENGTH_SHORT).show();
            recreate();
            return;
        } else if (timePlayed < 10) {
            result = TestResultActivity.Grade.A_PLUS;
            frequency = 18000;
        } else if (timePlayed < 20) {
            frequency = 18000;
            result = TestResultActivity.Grade.A;
        } else if (timePlayed < 30) {
            frequency = 17400;
            result = TestResultActivity.Grade.B_PLUS;
        } else if (timePlayed < 40) {
            frequency = 17400;
            result = TestResultActivity.Grade.B;
        } else if (timePlayed < 50) {
            frequency = 14000;
            result = TestResultActivity.Grade.C_PLUS;
        } else if (timePlayed < 60) {
            frequency = 14000;
            result = TestResultActivity.Grade.C;
        } else if (timePlayed < 70) {
            frequency = 8000;
            result = TestResultActivity.Grade.D_PLUS;
        } else if (timePlayed < 80) {
            frequency = 8000;
            result = TestResultActivity.Grade.D;
        } else {
            result = TestResultActivity.Grade.F;
        }

        var points = 100 - timePlayed;
        var intent = new Intent(this, TestResultActivity.class);
        intent.putExtra("grade", result);
        intent.putExtra("points", points);
        intent.putExtra("frequency", frequency);
        startActivity(intent);
    }


    @Override
    protected void onStop() {
        super.onStop();
        player.release();
    }
}
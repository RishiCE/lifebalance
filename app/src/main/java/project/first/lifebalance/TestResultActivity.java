package project.first.lifebalance;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

public class TestResultActivity extends AppCompatActivity {
    public ImageView gif;

    enum Grade {
        A_PLUS,A,B_PLUS,B,C_PLUS,C,D_PLUS,D,F
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);

        CircularProgressIndicator progress = findViewById(R.id.progress);
        TextView gradeView = findViewById(R.id.gradeView);
        TextView frequency = findViewById(R.id.frequency);
        TextView tip = findViewById(R.id.tip);
        gif = findViewById(R.id.gif);


        var grade = (Grade) getIntent().getSerializableExtra("grade");
        var progressValue = getIntent().getIntExtra("points", 0);
        var freq = getIntent().getIntExtra("frequency",1000);

        progress.setProgress(progressValue, 100);
        gradeView.append(grade.toString().replace("_PLUS","+"));

        String tipStr = "";
        if (grade == Grade.A || grade == Grade.A_PLUS){
            gradeView.setTextColor(Color.GREEN);
            tipStr = getResources().getString(R.string.tip_good);
        } else if (grade == Grade.B || grade == Grade.B_PLUS) {
            gradeView.setTextColor(Color.parseColor("#FFA500"));
            tipStr = getResources().getString(R.string.tip_average);
        } else if (grade == Grade.C || grade == Grade.C_PLUS) {
            gradeView.setTextColor(Color.YELLOW);
            tipStr = getResources().getString(R.string.tip_bad);
        } else if (grade == Grade.D || grade == Grade.D_PLUS || grade == Grade.F) {
            gradeView.setTextColor(Color.RED);
            tipStr = getResources().getString(R.string.tip_bad);
        }

        frequency.append(freq + "Hz");
        tip.setText(String.format(tip.getText().toString(),tipStr));

        if (grade == Grade.A_PLUS || grade == Grade.A && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            showRibbons();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    private void showRibbons(){
        try {
            ImageDecoder.Source source = ImageDecoder.createSource(getResources(), R.drawable.congo);
            @SuppressLint("WrongThread") Drawable drawable = ImageDecoder.decodeDrawable(source);
            gif.setImageDrawable(drawable);
            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).start();
                new Handler().postDelayed(() -> ((AnimatedImageDrawable) drawable).stop(),2000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
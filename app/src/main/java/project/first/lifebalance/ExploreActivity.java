package project.first.lifebalance;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ExploreActivity extends AppCompatActivity {
    protected int previousPlayedIndex = -1;
    protected Uri ringtone;
    protected MediaPlayer player = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        LinearLayout container = findViewById(R.id.audiolayout);
        var audios = container.getChildCount();
        for (int i = 0; i < audios; i++) {
            AudioSection audioSection = (AudioSection) container.getChildAt(i);
            int finalI = i;
            audioSection.ringtoneSetter = new AudioSection.RingtoneSetter() {
                @Override
                public void setRingtone(Uri ringtone) {
                    ExploreActivity.this.ringtone = ringtone;
                    var intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, 1);
                }

                @Override
                public void restoreRingtone(Uri contact,Uri ringtone) {
                    ExploreActivity.this.ringtone = ringtone;
                    AudioSection.setRingtone(ExploreActivity.this, contact, ringtone);
                    Toast.makeText(ExploreActivity.this, "Restored!", Toast.LENGTH_SHORT).show();
                }
            };

            audioSection.play.setOnClickListener(v -> {
                if (previousPlayedIndex == -1) {
                    // no audio is playing
                    audioSection.play(player);
                    previousPlayedIndex = finalI;
                } else {
                    // another audio is playing
                    if (previousPlayedIndex == finalI) { // the same audio is playing
                        audioSection.stop(player);
                        previousPlayedIndex = -1;
                    } else {
                        ((AudioSection) container.getChildAt(previousPlayedIndex)).stop(player);
                        audioSection.play(player);
                        previousPlayedIndex = finalI;
                    }
                }
            });
        }

        player.setOnCompletionListener(mp -> {
            ((AudioSection) container.getChildAt(previousPlayedIndex)).stop(player);
            previousPlayedIndex = -1;
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK){
            AudioSection.setRingtone(this, ringtone, data.getData());
            Toast.makeText(this, "Ringtone set for this contact", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
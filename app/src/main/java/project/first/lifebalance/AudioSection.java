package project.first.lifebalance;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.io.IOException;
import java.io.OutputStream;

public class AudioSection extends FrameLayout {
    private AssetFileDescriptor audio;
    public RingtoneSetter ringtoneSetter;
    public ImageView play,menu;
    public VideoView wave;
    public boolean isPlaying = false;

    public AudioSection(@NonNull Context context) {
        super(context);
    }

    public AudioSection(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public AudioSection(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    public AudioSection(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs){
        var view = inflate(context, R.layout.audio_section,this);
        TextView title = view.findViewById(R.id.title);
        wave = view.findViewById(R.id.wave);
        menu = view.findViewById(R.id.menu);
        play = view.findViewById(R.id.play);

        wave.setVideoURI(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.waveform));
        wave.setOnPreparedListener(mp -> mp.setLooping(true));

        var attributes = context.obtainStyledAttributes(attrs,R.styleable.AudioSection);
        var titleStr = attributes.getString(R.styleable.AudioSection_title);
        var rawId = attributes.getResourceId(R.styleable.AudioSection_audio,0);
        audio = context.getResources().openRawResourceFd(rawId);
        title.setText(titleStr);

        menu.setOnClickListener(v -> {
            var pref = context.getSharedPreferences("ringtone",Context.MODE_PRIVATE);
            var menu = new PopupMenu(context,v);
            menu.getMenu().add(0,1,0,"Set as Ringtone");
            menu.getMenu().add(0,0,1,"Restore ringtone");
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1){
                    String ringtone;
                    if (!pref.getBoolean(titleStr,false)){
                        ringtone = copyRingtoneToSystem(rawId,titleStr,context).toString();
                        var editor = pref.edit();
                        editor.putString("ringtone", ringtone);
                        editor.putBoolean(titleStr,true);
                        editor.apply();
                    } else
                        ringtone = pref.getString("ringtone","");

                    ringtoneSetter.setRingtone(Uri.parse(ringtone));
                } else {
                    var contact = pref.getString("lastContact_" + titleStr,null);
                    var ringtone = RingtoneManager.getActualDefaultRingtoneUri(context,RingtoneManager.TYPE_RINGTONE);
                    ringtoneSetter.restoreRingtone(Uri.parse(contact),ringtone);
                }
                return true;
            });
            menu.show();
        });

        attributes.recycle();
    }

    public void play(MediaPlayer player){
        isPlaying = true;
        wave.setVisibility(View.VISIBLE);
        wave.start();
        play.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.paue,null));
        try {
            player.setDataSource(audio);
            player.prepare();
            player.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop(MediaPlayer player){
        isPlaying = false;
        player.stop();
        player.reset();
        wave.pause();
        wave.setVisibility(View.INVISIBLE);
        play.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.play,null));
    }

    private Uri copyRingtoneToSystem(int rawFile,String title,Context context){
        ContentResolver resolver = context.getContentResolver();
        var values = new ContentValues();
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_RINGTONES);
        values.put(MediaStore.MediaColumns.DISPLAY_NAME,title + ".mp3");
        values.put(MediaStore.MediaColumns.TITLE,title);
        values.put(MediaStore.MediaColumns.MIME_TYPE,"audio/mp3");
        values.put(MediaStore.Audio.Media.IS_RINGTONE,true);
        var newUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,values);

        try {
            var inputStream = getResources().openRawResource(rawFile);
            OutputStream fileOutputStream = getContext().getContentResolver().openOutputStream(newUri);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
            inputStream.close();
            fileOutputStream.close();
            return newUri;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "IOExcpetion : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static void setRingtone(Context context,Uri contact,Uri ringtone){
        var values = new ContentValues();
        values.put(ContactsContract.Contacts.CUSTOM_RINGTONE,ringtone.toString());
        context.getContentResolver().update(contact,values,null,null);

        context.getSharedPreferences("ringtone",Context.MODE_PRIVATE)
                .edit()
                .putString("lastContact_" + getFileName(ringtone,context),contact.toString())
                .apply();
    }

    public static String getFileName(Uri uri,Context context){
        var cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME},null,null,null);
        if (cursor != null && cursor.moveToFirst()){
            @SuppressLint("Range") var name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            cursor.close();
            return name;
        }
        return "default";
    }

    public interface RingtoneSetter {
        void setRingtone(Uri ringtone);
        void restoreRingtone(Uri contact,Uri ringtone);
    }
}

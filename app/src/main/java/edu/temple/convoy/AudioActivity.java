package edu.temple.convoy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AudioActivity extends AppCompatActivity {

    private static int MICRO_PERMISSION = 200;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Boolean playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        playing = false;

        if(micPresent()){
            getPerms();
        }
    }

    public void startRecClick(View view) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        String check = settings.getString("convoyID", null);

        if(check!=null && !playing){

            try{
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setOutputFile(getRecording());
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.prepare();
                mediaRecorder.start();
                Toast.makeText(AudioActivity.this, "RECORDING STARTED", Toast.LENGTH_SHORT).show();
                playing=true;
            }catch(Exception e){
                 e.printStackTrace();
            }

        }else{
            Toast.makeText(AudioActivity.this, "Must Be In Active Convoy To Record", Toast.LENGTH_SHORT).show();
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Toast.makeText(AudioActivity.this, "RECORDING STOPPED", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelClick(View view) {
        finish();
    }

    private boolean micPresent(){
        if(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            return true;
        }else{
            return false;
        }
    }

    private void getPerms(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICRO_PERMISSION);
        }
    }

    //file storage
    private String getRecording(){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDir, "fileName.mp3");
        return file.getPath();
    }

    public void playRecord(View view) {

        try{
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getRecording());
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(AudioActivity.this, "RECORDING PLAYING", Toast.LENGTH_SHORT).show();

        }catch(Exception e){
            e.printStackTrace();
        }


    }

    public void sendRecording(){

    }
}
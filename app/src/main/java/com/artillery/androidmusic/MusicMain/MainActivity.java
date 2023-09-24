package com.artillery.androidmusic.MusicMain;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.artillery.music.R;
import com.artillery.musicmain.ui.MusicMainActivity;

/**
 * @author ArtilleryOrchid
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(this, MusicMainActivity.class));
    }
}
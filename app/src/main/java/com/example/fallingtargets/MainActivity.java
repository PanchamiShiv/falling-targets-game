package com.example.fallingtargets;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView highScoreText;
    private MediaPlayer menuMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        highScoreText = findViewById(R.id.highScoreText);
        Button btnStartGame = findViewById(R.id.btnStartGame);
        Button btnSettings = findViewById(R.id.btnSettings);

        // Show saved high score
        int best = Prefs.getHigh(this);
        highScoreText.setText("High Score: " + best);

        // Start Game → show instructions first
        btnStartGame.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("How to Play")
                    .setMessage(
                            "🎯 Falling Targets\n\n" +
                                    "• Tilt your phone → Move the cannon left/right\n" +
                                    "• Tap your finger on the screen → Shoot bullets\n" +
                                    "• Destroy falling balls before they touch the ground\n" +
                                    "• You have 3 lives. When lives = 0 → Game Over\n" +
                                    "• Score points and build combos for higher scores!\n\n" +
                                    "✨ Special Move:\n" +
                                    "Quickly twist (flick) your phone left or right to activate the special move.\n" +
                                    "This instantly clears ALL falling balls from the screen!\n" +
                                    " ⏸️ Pause/Resume:\n"+
                                    "Pause anytime by simply covering the small sensor at the top of your phone (next to the front camera).\n\n"+
                                    "Good luck and have fun!"
                    )
                    .setPositiveButton("OK", (dialog, which) -> {
                        stopMenuMusic();
                        Intent i = new Intent(MainActivity.this, GameActivity.class);
                        startActivity(i);
                    })
                    .setCancelable(false)
                    .show();
        });

        // Settings
        btnSettings.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh high score
        int best = Prefs.getHigh(this);
        highScoreText.setText("High Score: " + best);

        // Play menu music if not muted
        if (!Prefs.isMuted(this)) {
            if (menuMusic == null) {
                menuMusic = MediaPlayer.create(this, R.raw.bg_music); // ✅ fixed
                menuMusic.setLooping(true);
            }
            menuMusic.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMenuMusic();
    }

    private void stopMenuMusic() {
        if (menuMusic != null) {
            if (menuMusic.isPlaying()) menuMusic.stop();
            menuMusic.release();
            menuMusic = null;
        }
    }
}

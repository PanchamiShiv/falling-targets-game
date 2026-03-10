package com.example.fallingtargets;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    private MediaPlayer gameOverMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        TextView scoreText = findViewById(R.id.scoreText);
        TextView highScoreText = findViewById(R.id.highScoreText);
        Button btnNewGame = findViewById(R.id.btnNewGame);
        Button btnMainMenu = findViewById(R.id.btnMainMenu);

        // Get score and high score from intent
        int score = getIntent().getIntExtra("score", 0);
        int highScore = getIntent().getIntExtra("highScore", 0);

        scoreText.setText("Your Score: " + score);
        highScoreText.setText("High Score: " + highScore);

        // Start game over music if not muted
        if (!Prefs.isMuted(this)) {
            gameOverMusic = MediaPlayer.create(this, R.raw.game_over);
            gameOverMusic.setLooping(false); // play once
            gameOverMusic.start();
        }

        // Button to start a new game
        btnNewGame.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(GameOverActivity.this, GameActivity.class));
            finish();
        });

        // Button to go back to main menu
        btnMainMenu.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(GameOverActivity.this, MainActivity.class));
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    private void stopMusic() {
        if (gameOverMusic != null) {
            if (gameOverMusic.isPlaying()) {
                gameOverMusic.stop();
            }
            gameOverMusic.release();
            gameOverMusic = null;
        }
    }
}

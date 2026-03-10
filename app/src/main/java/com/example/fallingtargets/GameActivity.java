package com.example.fallingtargets;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accel, gyro, proximity;
    private GameView gameView;

    private TextView scoreText, livesText;

    // Gyroscope flick detection threshold
    private static final float FLICK_THRESHOLD_Z = 2.2f;

    // Proximity state
    private Boolean isNear = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        FrameLayout container = findViewById(R.id.gameContainer);
        scoreText = findViewById(R.id.scoreText);
        livesText = findViewById(R.id.livesText);

        // GameView with event listener
        gameView = new GameView(this, new GameView.GameEventListener() {
            @Override
            public void onScoreChanged(int newScore) {
                runOnUiThread(() -> scoreText.setText("Score: " + newScore));
            }

            @Override
            public void onLivesChanged(int newLives) {
                runOnUiThread(() -> livesText.setText("Lives: " + newLives));
            }

            @Override
            public void onGameOver(int finalScore) {
                int high = Prefs.updateHighIfGreater(GameActivity.this, finalScore);

                runOnUiThread(() -> {
                    // Go to GameOverActivity instead of showing dialog
                    Intent intent = new Intent(GameActivity.this, GameOverActivity.class);
                    intent.putExtra("score", finalScore);
                    intent.putExtra("highScore", high);
                    startActivity(intent);
                    finish();
                });
            }
        });

        container.addView(gameView);

        // Sensor setup
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accel != null) sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
        if (gyro != null) sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
        if (proximity != null) sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_GAME);
        gameView.resumeLoop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        gameView.pauseLoop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) {
            gameView.release(); // cleanup sounds
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            gameView.setTilt(-x);

        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float z = event.values[2];
            if (Math.abs(z) > FLICK_THRESHOLD_Z) {
                activateSpecialMove();
            }

        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            boolean nowNear = distance < event.sensor.getMaximumRange();

            if (isNear == null) {
                isNear = nowNear;
                return;
            }

            if (nowNear && !isNear) {
                // FAR → NEAR → Pause and show dialog
                runOnUiThread(() -> {
                    gameView.pauseLoop();
                    showPauseDialog();
                });
            }

            // Only pause with dialog; resume happens when user taps "Continue"
            isNear = nowNear;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // Gyroscope special move
    private void activateSpecialMove() {
        runOnUiThread(() -> {
            gameView.clearTargets();
            Toast.makeText(GameActivity.this, "SPECIAL MOVE!", Toast.LENGTH_SHORT).show();
        });
    }

    // Pause dialog
    private void showPauseDialog() {
        new AlertDialog.Builder(GameActivity.this)
                .setTitle("Game Paused")
                .setMessage("Do you want to continue or go back to the main menu?")
                .setCancelable(false)
                .setPositiveButton("Continue", (dialog, which) -> {
                    gameView.resumeLoop();
                })
                .setNegativeButton("Main Menu", (dialog, which) -> {
                    Intent i = new Intent(GameActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                })
                .show();
    }
}

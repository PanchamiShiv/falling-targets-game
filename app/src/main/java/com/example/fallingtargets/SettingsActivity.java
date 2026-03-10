package com.example.fallingtargets;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar sensitivityBar;
    private Switch hapticsSwitch, muteSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sensitivityBar = findViewById(R.id.seekSensitivity);
        hapticsSwitch = findViewById(R.id.switchHaptics);
        muteSwitch = findViewById(R.id.switchMute);

        // Load saved settings
        sensitivityBar.setProgress((int) Prefs.getSensitivity(this));
        hapticsSwitch.setChecked(Prefs.getHaptics(this));
        muteSwitch.setChecked(Prefs.isMuted(this));

        // Save changes
        sensitivityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Prefs.setSensitivity(SettingsActivity.this, progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        hapticsSwitch.setOnCheckedChangeListener((btn, isChecked) ->
                Prefs.setHaptics(SettingsActivity.this, isChecked));

        muteSwitch.setOnCheckedChangeListener((btn, isChecked) ->
                Prefs.setMuted(SettingsActivity.this, isChecked));
    }
}

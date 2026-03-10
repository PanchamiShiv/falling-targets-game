# Falling Targets – Sensor-Based Android Game

Falling Targets is an Android arcade-style shooting game developed in Java using Android Studio.  
The player controls a cannon at the bottom of the screen and must destroy falling targets before they reach the ground.

The game integrates **three mobile sensors (Accelerometer, Gyroscope, and Proximity Sensor)** to create an interactive gameplay experience.

---

## Game Overview

In Falling Targets, targets continuously fall from the top of the screen. The player must destroy them before they reach the bottom.

The game combines:

- Sensor-based movement
- Touch-based shooting
- Special gesture mechanics
- Real-time collision detection
- Score and combo systems

If too many targets reach the ground, the player loses lives and the game ends.

---

## Features

- Sensor-based gameplay using mobile sensors
- Accelerometer-controlled cannon movement
- Touch-based shooting system
- Gyroscope-triggered special move
- Proximity-sensor pause control
- Real-time target spawning
- Collision detection system
- Combo scoring system
- Sound effects and background music
- Haptic vibration feedback
- Persistent high score storage
- Adjustable game settings

---

## Sensors Used

### Accelerometer
The accelerometer is used to detect the tilt of the device.

Tilting the phone left or right moves the cannon horizontally across the screen.  
This allows the player to control the cannon without using on-screen buttons.

---

### Gyroscope
The gyroscope detects rotational movement of the device.

When the player performs a quick flick motion with the phone, the game activates a **special move** that clears all targets currently on the screen.

This helps the player escape difficult situations.

---

### Proximity Sensor
The proximity sensor detects objects close to the phone’s front sensor.

When the player covers the proximity sensor, the game automatically **pauses** and shows a pause menu with options to continue the game or return to the main menu.

---

## Gameplay Mechanics

### Shooting
The player taps or holds the screen to fire bullets from the cannon.

Bullets travel upward and destroy targets when they collide.

---

### Targets
Targets spawn randomly at the top of the screen and fall toward the bottom with different speeds.

If a target reaches the ground, the player loses a life.

---

### Lives System
The player starts with several lives.

Each missed target reduces the number of lives.

When all lives are lost, the **Game Over screen** is displayed.

---

### Combo Scoring System
The game includes a combo scoring mechanism.

If the player destroys multiple targets quickly, the combo count increases and the score multiplier becomes higher.

Example scoring:

| Hit | Points |
|----|----|
| First hit | 10 |
| Second hit | 20 |
| Third hit | 30 |

Combos reward fast and accurate gameplay.

---

## Sound and Feedback

The game uses two audio systems.

### Background Music
Handled using **MediaPlayer** for menu music and gameplay background music.

### Sound Effects
Handled using **SoundPool** for effects such as:

- Shooting
- Target hit
- Life lost
- Special move activation
- Game over sound

---

### Haptic Feedback
When a target is destroyed, the device vibrates briefly to provide tactile feedback.

This feature can be enabled or disabled in the settings menu.

---
### Working

Open the project in **Android Studio**.

---

## Running the Application

1. Open the project in Android Studio  
2. Connect an Android device or start an emulator  
3. Click **Run**

The game will launch on the device.

---

## Requirements

- Android Studio
- Android SDK
- Android device with sensors:
  - Accelerometer
  - Gyroscope
  - Proximity Sensor

---

## Game Controls

| Action | Control |
|------|------|
| Move Cannon | Tilt phone left or right |
| Shoot | Touch screen |
| Special Move | Flick the phone quickly |
| Pause Game | Cover proximity sensor |

---

## Future Improvements

Possible future improvements include:

- Multiple difficulty levels
- New target types and power-ups
- Leaderboard system
- Improved graphics and animations
- Online score tracking

---

## License

This project was developed for educational purposes.


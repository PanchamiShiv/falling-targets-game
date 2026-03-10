package com.example.fallingtargets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    public interface GameEventListener {
        void onScoreChanged(int newScore);
        void onLivesChanged(int newLives);
        void onGameOver(int finalScore);
    }

    private final GameEventListener listener;

    private Thread loopThread;
    private boolean running = false;
    private final SurfaceHolder holder = getHolder();

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Cannon
    private float cannonX;
    private static final float CANNON_WIDTH = 120f;
    private static final float CANNON_HEIGHT = 30f;
    private static final float BARREL_HEIGHT = 40f;

    // Input
    private float tiltX = 0f;
    private float sensitivity = 18f;
    private boolean paused = false;
    private boolean isFiring = false;

    // Haptics
    private final Vibrator vibrator;

    // Bullets
    private static class Bullet {
        float x, y, vy;
        Bullet(float x, float y, float vy) { this.x = x; this.y = y; this.vy = vy; }
    }
    private final List<Bullet> bullets = new ArrayList<>();
    private long lastFireMs = 0;
    private static final long FIRE_COOLDOWN_MS = 200; // 5 bullets/sec

    // Targets
    private static class Target {
        float x, y, vy, r;
        Target(float x, float y, float vy, float r) { this.x=x; this.y=y; this.vy=vy; this.r=r; }
    }
    private final List<Target> targets = new ArrayList<>();
    private final Random rng = new Random();
    private long lastSpawnMs = 0;
    private static final int BASE_INTERVAL = 800; // fixed spawn rate
    private static final float BASE_SPEED = 250;  // fixed speed baseline

    // Score & Lives
    private int score = 0;
    private int lives = 3;

    // Combo
    private int comboCount = 0;
    private long lastHitTime = 0;
    private static final long COMBO_TIMEOUT_MS = 2000;

    // === Audio ===
    private MediaPlayer bgMusic;
    private SoundPool soundPool;
    private int soundShoot, soundHit, soundLifeLost, soundSpecial, soundGameOver;

    public GameView(Context context, GameEventListener listener) {
        super(context);
        this.listener = listener;
        holder.addCallback(this);
        setFocusable(true);
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        // Background music
        bgMusic = MediaPlayer.create(context, R.raw.bg_music);
        if (bgMusic != null) bgMusic.setLooping(true);

        // Sound effects
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build();

        soundShoot   = soundPool.load(context, R.raw.shoot, 1);
        soundHit     = soundPool.load(context, R.raw.hit, 1);
        soundLifeLost= soundPool.load(context, R.raw.life_lost, 1);
        soundSpecial = soundPool.load(context, R.raw.special, 1);
        soundGameOver= soundPool.load(context, R.raw.game_over, 1);
    }

    // === Input ===
    public void setTilt(float t) { this.tiltX = clamp(t, -5f, 5f); }

    private void fireBullet() {
        long now = System.currentTimeMillis();
        if (now - lastFireMs < FIRE_COOLDOWN_MS || paused) return;
        lastFireMs = now;

        float gunTop = getHeight() - 60f - BARREL_HEIGHT;
        bullets.add(new Bullet(cannonX, gunTop, -900f));

        if (!Prefs.isMuted(getContext())) {
            soundPool.play(soundShoot, 1f, 1f, 0, 0, 1f);
        }
    }

    public void clearTargets() {
        targets.clear();
        bullets.clear();
        comboCount = 0;

        if (!Prefs.isMuted(getContext())) {
            soundPool.play(soundSpecial, 1f, 1f, 0, 0, 1f);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isFiring = true;
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isFiring = false;
                return true;
        }
        return super.onTouchEvent(event);
    }


    public void resumeLoop() {
        running = true;
        loopThread = new Thread(this);
        loopThread.start();

        if (bgMusic != null && !bgMusic.isPlaying() && !Prefs.isMuted(getContext())) {
            bgMusic.start();
        }
    }

    public void pauseLoop() {
        running = false;
        if (loopThread != null) {
            try { loopThread.join(); } catch (InterruptedException ignored) {}
            loopThread = null;
        }
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    public void release() {
        if (bgMusic != null) {
            bgMusic.release();
            bgMusic = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {}
    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    @Override public void surfaceDestroyed(SurfaceHolder holder) { pauseLoop(); }

    @Override
    public void run() {
        long prev = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float dt = (now - prev) / 1_000_000_000f;
            prev = now;

            if (!paused) update(dt);
            drawFrame();
        }
    }

    private void update(float dt) {
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        if (cannonX == 0f) cannonX = w / 2f;
        cannonX += tiltX * sensitivity;
        cannonX = clamp(cannonX, 40f, w - 40f);

        if (isFiring) fireBullet();

        // Fixed spawn rate
        long now = System.currentTimeMillis();
        if (now - lastSpawnMs > BASE_INTERVAL) {
            float x = 40 + rng.nextFloat() * (w - 80);
            float vy = BASE_SPEED + rng.nextFloat() * 150;
            float r  = 22 + rng.nextInt(14);
            targets.add(new Target(x, -30f, vy, r));
            lastSpawnMs = now;
        }

        // Update targets
        Iterator<Target> itTargets = targets.iterator();
        while (itTargets.hasNext()) {
            Target t = itTargets.next();
            t.y += t.vy * dt;
            if (t.y + t.r >= h - 20f) {
                itTargets.remove();
                lives--;
                comboCount = 0;

                if (listener != null) listener.onLivesChanged(lives);

                if (lives > 0) {
                    if (!Prefs.isMuted(getContext())) {
                        soundPool.play(soundLifeLost, 1f, 1f, 0, 0, 1f);
                    }
                } else {
                    if (!Prefs.isMuted(getContext())) {
                        soundPool.play(soundGameOver, 1f, 1f, 0, 0, 1f);
                    }
                    running = false;
                    if (listener != null) listener.onGameOver(score);
                    return;
                }
            }
        }

        // Update bullets
        Iterator<Bullet> itB = bullets.iterator();
        while (itB.hasNext()) {
            Bullet b = itB.next();
            b.y += b.vy * dt;
            if (b.y < -30f) itB.remove();
        }

        // Collisions
        Iterator<Target> itT = targets.iterator();
        while (itT.hasNext()) {
            Target t = itT.next();
            boolean hit = false;
            for (Iterator<Bullet> bb = bullets.iterator(); bb.hasNext();) {
                Bullet b = bb.next();
                float dx = b.x - t.x, dy = b.y - t.y;
                if (dx*dx + dy*dy <= t.r * t.r) {
                    bb.remove();
                    hit = true;

                    long nowHit = System.currentTimeMillis();
                    if (nowHit - lastHitTime <= COMBO_TIMEOUT_MS) {
                        comboCount++;
                    } else {
                        comboCount = 1;
                    }
                    lastHitTime = nowHit;

                    score += 10 * comboCount;
                    if (listener != null) listener.onScoreChanged(score);

                    if (!Prefs.isMuted(getContext())) {
                        soundPool.play(soundHit, 1f, 1f, 0, 0, 1f);
                    }

                    if (Prefs.getHaptics(getContext()) && vibrator != null) {
                        if (android.os.Build.VERSION.SDK_INT >= 26) {
                            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(30);
                        }
                    }
                    break;
                }
            }
            if (hit) itT.remove();
        }
    }

    private void drawFrame() {
        Canvas c = holder.lockCanvas();
        if (c == null) return;

        c.drawColor(Color.rgb(18, 18, 22));

        // Ground
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(6f);
        c.drawLine(0, getHeight() - 20f, getWidth(), getHeight() - 20f, paint);

        // Cannon
        paint.setColor(Color.CYAN);
        float bodyTop = getHeight() - 60f;
        c.drawRect(cannonX - CANNON_WIDTH/2f, bodyTop, cannonX + CANNON_WIDTH/2f, bodyTop + CANNON_HEIGHT, paint);
        c.drawRect(cannonX - 6f, bodyTop - BARREL_HEIGHT, cannonX + 6f, bodyTop, paint);

        // Targets
        paint.setColor(Color.YELLOW);
        for (Target t : targets) {
            c.drawCircle(t.x, t.y, t.r, paint);
        }

        // Bullets
        paint.setColor(Color.WHITE);
        for (Bullet b : bullets) {
            c.drawRect(b.x - 3f, b.y - 10f, b.x + 3f, b.y + 10f, paint);
        }

        // Combo text
        if (comboCount > 1) {
            paint.setColor(Color.MAGENTA);
            paint.setTextSize(70f);
            c.drawText("Combo x" + comboCount, getWidth()/2f - 150f, 160f, paint);
        }

        holder.unlockCanvasAndPost(c);
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}

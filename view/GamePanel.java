package view;

import javax.swing.*;

import controller.KeyEventListener;
import model.Bullet;
import model.Enemy;
import model.Explosion;
import model.Player;
import model.PowerUp;
import model.Text;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {

    // FIELDS
    public static int WIDTH = 400;
    public static int HEIGHT = 400;

    private Thread thread;
    private boolean running;

    
    private BufferedImage image;
    private Graphics2D g2;

    private int FPS = 30;
    private double averageFPS;

    public static Player player;
    public static ArrayList<Bullet> bullets;
    public static ArrayList<Enemy> enemies;
    public static ArrayList<PowerUp> powerUps;
    public static ArrayList<Explosion> explosions;
    public static ArrayList<Text> texts;

    private long waveStartTimer;
    private long waveStartTimerDiff;
    private int waveNumber;
    private boolean waveStart;
    private int waveDelay = 2000;

    private long slowDownTimer;
    private long slowDownTimerDiff;
    private int slowDownLength = 6000;

    // CONSTRUCTOR
    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
    }

    KeyEventListener listener = new KeyEventListener(this);
    // FUNCTIONS
    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
        addKeyListener(listener);
    }

    public void run() {

        running = true;

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g2 = (Graphics2D) image.getGraphics();

        // Make graphics not so blocky
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        player = new Player();
        bullets = new ArrayList<Bullet>();
        enemies = new ArrayList<Enemy>();
        powerUps = new ArrayList<PowerUp>();
        explosions = new ArrayList<Explosion>();
        texts = new ArrayList<Text>();

        waveStartTimer = 0;
        waveStartTimerDiff = 0;
        waveStart = true;
        waveNumber = 0;

        long startTime;
        long URDTimeMillis;
        long waitTime;
        long totalTime = 0;

        int frameCount = 0;
        int maxFrameCount = 30;

        long targetTime = 1000 / FPS;

        // GAME LOOP
        while(running) {

            startTime = System.nanoTime();

            gameUpdate();
            gameRender();
            gameDraw();

            URDTimeMillis = (System.nanoTime() - startTime) / 1000000;

            waitTime = targetTime - URDTimeMillis;

            try {
                Thread.sleep(waitTime);
            } catch (Exception e) {
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;
            if (frameCount == maxFrameCount) {
                averageFPS = 1000.0 / ((totalTime / frameCount) / 1000000);
                frameCount = 0;
                totalTime = 0;
            }
        }

        g2.setColor(new Color(0, 0, 0));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Century Gothic", Font.PLAIN, 16));
        String s = "G A M E   O V E R";
        int length = (int) g2.getFontMetrics().getStringBounds(s, g2).getWidth();
        g2.drawString(s, (WIDTH - length) / 2, HEIGHT / 2);
        s = "Final Score: " + player.getScore();
        length = (int) g2.getFontMetrics().getStringBounds(s, g2).getWidth();
        g2.drawString(s, (WIDTH - length) / 2, HEIGHT / 2 + 30);
        gameDraw();
    }

    private void gameUpdate() {

        // new wave
        if (waveStartTimer == 0 && enemies.size() == 0) {
            ++waveNumber;
            waveStart = false;
            waveStartTimer = System.nanoTime();
        } else {
            waveStartTimerDiff = (System.nanoTime() - waveStartTimer) / 1000000;
            if (waveStartTimerDiff > waveDelay) {
                waveStart = true;
                waveStartTimer = 0;
                waveStartTimerDiff = 0;
            }
        }

        // Create enemies
        if (waveStart && enemies.size() == 0) {
            createNewEnemies();
        }

        // Player update
        player.update();

        // Bullet update
        for (int i = 0; i < bullets.size(); ++i) {
            boolean remove = bullets.get(i).update();
            if (remove) {
                bullets.remove(i);
                --i;
            }
        }

        // Enemy update
        for (int i = 0; i < enemies.size(); ++i) {
            enemies.get(i).update();
        }

        // Powerup update
        for (int i = 0; i < powerUps.size(); i++) {
            boolean remove = powerUps.get(i).update();
            if (remove) {
                powerUps.remove(i);
                --i;
            }
        }

        // Explosion update
        for (int i = 0; i < explosions.size(); ++i) {
            boolean remove = explosions.get(i).update();
            if (remove) {
                explosions.remove(i);
                --i;
            }
        }

        // Text update
        for (int i = 0; i < texts.size(); ++i) {
            boolean remove = texts.get(i).update();
            if (remove) {
                texts.remove(i);
                --i;
            }
        }

        // Bullet-enemy collision
        for (int i = 0; i < bullets.size(); ++i) {

            Bullet b = bullets.get(i);
            double bx = b.getX();
            double by = b.getY();
            double br = b.getR();

            for (int j = 0; j < enemies.size(); ++j) {

                Enemy e = enemies.get(j);
                double ex = e.getX();
                double ey = e.getY();
                double er = e.getR();

                double dx = bx - ex;
                double dy = by - ey;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < br + er) {
                    e.hit();
                    bullets.remove(i);
                    i--;
                    break;
                }
            }
        }

        // Check dead enemies
        for (int i = 0; i < enemies.size(); ++i) {

            if (enemies.get(i).isDead()) {

                Enemy e = enemies.get(i);

                // Chance for powerup
                double rand = Math.random();
                if (rand < 0.001) powerUps.add(new PowerUp(1, e.getX(), e.getY()));
                else if (rand < 0.020) powerUps.add(new PowerUp(3, e.getX(), e.getY()));
                else if (rand < 0.120) powerUps.add(new PowerUp(2, e.getX(), e.getY()));
                else if (rand < 0.130) powerUps.add(new PowerUp(4, e.getX(), e.getY()));

                player.addScore(e.getRank() + e.getType());
                enemies.remove(i);
                --i;

                e.explode();
                explosions.add(new Explosion(e.getX(), e.getY(), e.getR(), e.getR() + 30));

            }
        }

        // Check dead player
        if (player.isDead()) {
            running = false;
        }

        // Player-enemy collision
        if (!player.isRecovering()) {
            int px = player.getX();
            int py = player.getY();
            int pr = player.getR();
            for (int i = 0; i < enemies.size(); ++i) {

                Enemy e = enemies.get(i);
                double ex = e.getX();
                double ey = e.getY();
                double er = e.getR();

                double dx = px - ex;
                double dy = py - ey;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < pr + er) {
                    player.loseLife();
                }
            }
        }

        // Player-powerup collision
        int px = player.getX();
        int py = player.getY();
        int pr = player.getR();
        for (int i = 0; i < powerUps.size(); ++i) {
            PowerUp p = powerUps.get(i);
            double x = p.getX();
            double y = p.getY();
            double r = p.getR();

            double dx = px - x;
            double dy = py - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            // Collected powerup
            if (dist < pr + r) {
                int type = p.getType();

                if (type == 1) {
                    player.gainLife();
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Extra Life"));
                }
                if (type == 2) {
                    player.increasePower(1);
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Power"));
                }
                if (type == 3) {
                    player.increasePower(2);
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Double Power"));
                }
                if (type == 4) {
                    slowDownTimer = System.nanoTime();
                    for (int j = 0; j < enemies.size(); ++j) {
                        enemies.get(j).setSlow(true);
                    }
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Slow Down"));
                }

                powerUps.remove(i);
                --i;
            }
        }

        // Slow update
        if (slowDownTimer != 0) {
            slowDownTimerDiff = (System.nanoTime() - slowDownTimer) / 1000000;
            if (slowDownTimerDiff > slowDownTimer) {
                slowDownTimer = 0;
                for (int j = 0; j < enemies.size(); ++j) {
                    enemies.get(j).setSlow(false);
                }
            }
        }
    }

    private void gameRender() {
        //Draw background
        g2.setColor(new Color(178, 0, 255));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw slowdown screen
        if (slowDownTimer != 0) {
            g2.setColor((new Color(255, 255, 255, 64)));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
        } else if (slowDownTimerDiff > slowDownTimer) {
            g2.setColor((new Color(178, 0, 255)));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
        }

        // Draw player
        player.draw(g2);

        // Draw bullet
        for (int i = 0; i < bullets.size(); ++i) {
            bullets.get(i).draw(g2);
        }

        // Draw enemy
        for (int i = 0; i < enemies.size(); ++i) {
            enemies.get(i).draw(g2);
        }

        // Draw powerups
        for (int i = 0; i < powerUps.size(); ++i) {
            powerUps.get(i).draw(g2);
        }

        // Draw explosions
        for (int i = 0; i < explosions.size(); ++i) {
            explosions.get(i).draw(g2);
        }

        // Draw text
        for (int i = 0; i < texts.size(); ++i) {
            texts.get(i).draw(g2);
        }

        // Draw wave number
        if (waveStartTimer != 0) {
            g2.setFont(new Font("Century Gothic", Font.PLAIN, 18));
            String s = "- W A V E    " + waveNumber + "   -";
            int length = (int) g2.getFontMetrics().getStringBounds(s, g2).getWidth();
            int alpha = (int) (255 * Math.sin(3.14 * waveStartTimerDiff / waveDelay));
            if (alpha > 255) alpha = 255;
            g2.setColor(new Color(255, 255, 255, alpha));
            g2.drawString(s, WIDTH / 2 - length / 2, HEIGHT / 2);
        }

        // Draw player lives
        for (int i = 0; i < player.getLives(); ++i) {
            g2.setColor(Color.WHITE);
            g2.fillOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.WHITE.darker());
            g2.drawOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);
            g2.setStroke(new BasicStroke(1));
        }

        // Draw player power
        g2.setColor(Color.YELLOW);
        g2.fillRect(20,40,player.getPower() * 8, 8);
        g2.setColor(Color.YELLOW.darker());
        g2.setStroke(new BasicStroke(2));
        for (int i = 0; i < player.getRequiredPower(); ++i) {
            g2.drawRect(20 + 8 * i, 40, 8, 8);
        }
        g2.setStroke(new BasicStroke(1));

        // Draw player score
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        g2.drawString("Score: " + player.getScore(), WIDTH - 100, 10);

        // Draw slow meter
        if (slowDownTimer != 0) {
            g2.setColor(Color.WHITE);
            g2.drawRect(20,60,100,8);
            g2.fillRect(20, 60,
                    (int) (100 - 100.0 * slowDownTimerDiff / slowDownLength), 8);
        }
    }

    private void gameDraw() {
        Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }

    private void createNewEnemies() {

        enemies.clear();

        if (waveNumber == 1) {
            for (int i = 0; i < 4; ++i) {
                enemies.add(new Enemy(1, 1));
            }
        }
        if (waveNumber == 2) {
            for (int i = 0; i < 8; ++i) {
                enemies.add(new Enemy(1, 1));
            }
        }
        if (waveNumber == 3) {
            for (int i = 0; i < 4; ++i) {
                enemies.add(new Enemy(2, 1));
            }
            enemies.add(new Enemy(1, 2));
            enemies.add(new Enemy(1, 2));
        }
        if (waveNumber == 4) {
            for (int i = 0; i < 4; ++i) {
                enemies.add(new Enemy(2, 1));
            }
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(1, 4));
        }
        if (waveNumber == 5) {
            enemies.add(new Enemy(1, 4));
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(2, 3));
        }
        if (waveNumber == 6) {
            for (int i = 0; i < 4; ++i) {
                enemies.add(new Enemy(1, 3));
            }
            enemies.add(new Enemy(2, 1));
            enemies.add(new Enemy(3, 1));
        }
        if (waveNumber == 7) {
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(2, 3));
            enemies.add(new Enemy(3, 3));
        }
        if (waveNumber == 8) {
            enemies.add(new Enemy(1, 4));
            enemies.add(new Enemy(2, 4));
            enemies.add(new Enemy(3, 4));
        }
        if (waveNumber == 9) {
            running = false;
        }
    }

    public static Player getPlayer() {
        return player;
    }
}
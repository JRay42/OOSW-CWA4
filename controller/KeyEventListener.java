package controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import model.Player;
import view.GamePanel;

public class KeyEventListener implements KeyListener {

    private GamePanel panel;

    public KeyEventListener(GamePanel panel) {
        this.panel = panel;
    }

    public void keyTyped(KeyEvent e) { }

    public void keyPressed(KeyEvent e) {
        Player player = panel.getPlayer();
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT) {
            player.setLeft(true);
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            player.setRight(true);
        }
        if (keyCode == KeyEvent.VK_UP) {
            player.setUp(true);
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            player.setDown(true);
        }
        if (keyCode == KeyEvent.VK_Z) {
            player.setFiring(true);
        }
    }

    public void keyReleased(KeyEvent e) {
        Player player = panel.getPlayer();
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT) {
            player.setLeft(false);
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            player.setRight(false);
        }
        if (keyCode == KeyEvent.VK_UP) {
            player.setUp(false);
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            player.setDown(false);
        }
        if (keyCode == KeyEvent.VK_Z) {
            player.setFiring(false);
        }
    }
    
}

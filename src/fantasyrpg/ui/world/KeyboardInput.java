package fantasyrpg.ui.world;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardInput implements KeyListener {

    public boolean upPressed;
    public boolean downPressed;
    public boolean leftPressed;
    public boolean rightPressed;
    private boolean interactPressed;

    // Track the order of key presses to prioritize the latest input
    public char lastHorizontal = ' '; // 'L' for Left, 'R' for Right
    public char lastVertical = ' ';   // 'U' for Up, 'D' for Down

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            upPressed = true;
            lastVertical = 'U';
        }

        if (code == KeyEvent.VK_S) {
            downPressed = true;
            lastVertical = 'D';
        }

        if (code == KeyEvent.VK_A) {
            leftPressed = true;
            lastHorizontal = 'L';
        }

        if (code == KeyEvent.VK_D) {
            rightPressed = true;
            lastHorizontal = 'R';
        }

        if (code == KeyEvent.VK_E) {
            interactPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            upPressed = false;
            if (downPressed) {
                lastVertical = 'D';
            }
        }

        if (code == KeyEvent.VK_S) {
            downPressed = false;
            if (upPressed) {
                lastVertical = 'U';
            }
        }

        if (code == KeyEvent.VK_A) {
            leftPressed = false;
            if (rightPressed) {
                lastHorizontal = 'R';
            }
        }

        if (code == KeyEvent.VK_D) {
            rightPressed = false;
            if (leftPressed) {
                lastHorizontal = 'L';
            }
        }

        if (code == KeyEvent.VK_E) {
            interactPressed = false;
        }
    }

    public boolean consumeInteractPressed() {

        if (!interactPressed) {
            return false;
        }

        interactPressed = false;
        return true;
    }
}

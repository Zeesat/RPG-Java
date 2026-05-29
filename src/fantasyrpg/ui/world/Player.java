package fantasyrpg.ui.world;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

public class Player {

    public static final int DRAW_SIZE = 64;
    public static final int SOLID_OFFSET_X = 8;
    public static final int SOLID_OFFSET_Y = 16;
    public static final int SOLID_WIDTH = 40;
    public static final int SOLID_HEIGHT = 40;

    GamePanel gp;

    KeyboardInput keyInput;

    int x = 100;
    int y = 100;

    int speed = 4;

    BufferedImage front;
    BufferedImage back;
    BufferedImage left;
    BufferedImage right;

    BufferedImage currentSprite;

    Rectangle solidArea;

    public Player(
            GamePanel gp,
            KeyboardInput keyInput
    ) {

        this.gp = gp;
        this.keyInput = keyInput;

        x = gp.mapLoader.spawnX;
        y = gp.mapLoader.spawnY;

        loadPlayerImages();

        solidArea = new Rectangle(
                x + SOLID_OFFSET_X,
                y + SOLID_OFFSET_Y,
                SOLID_WIDTH,
                SOLID_HEIGHT
        );
    }

    private void loadPlayerImages() {

        try {

            front = ImageIO.read(
                    new File("assets/player/front.png")
            );

            back = ImageIO.read(
                    new File("assets/player/back.png")
            );

            left = ImageIO.read(
                    new File("assets/player/left.png")
            );

            right = ImageIO.read(
                    new File("assets/player/right.png")
            );

            currentSprite = front;

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void update() {

        int nextX = x;
        int nextY = y;

        if (keyInput.upPressed) {

            nextY -= speed;

            currentSprite = back;
        }

        if (keyInput.downPressed) {

            nextY += speed;

            currentSprite = front;
        }

        if (keyInput.leftPressed) {

            nextX -= speed;

            currentSprite = left;
        }

        if (keyInput.rightPressed) {

            nextX += speed;

            currentSprite = right;
        }

        Rectangle nextArea = new Rectangle(
                nextX + SOLID_OFFSET_X,
                nextY + SOLID_OFFSET_Y,
                SOLID_WIDTH,
                SOLID_HEIGHT
        );

        boolean collision = false;

        for (CollisionBlock block : gp.collisions) {

            if (nextArea.intersects(block.rectangle)) {

                collision = true;
                break;
            }
        }

        if (!collision) {

            x = nextX;
            y = nextY;
        }
    }

    public void draw(Graphics2D g2) {

        g2.drawImage(
                currentSprite,
                x,
                y,
                DRAW_SIZE,
                DRAW_SIZE,
                null
        );
    }

    public void setPosition(int newX, int newY) {

        x = newX;
        y = newY;
        solidArea.setLocation(
                x + SOLID_OFFSET_X,
                y + SOLID_OFFSET_Y
        );
    }

    public int getSolidLeft() {

        return x + SOLID_OFFSET_X;
    }

    public int getSolidRight() {

        return getSolidLeft() + SOLID_WIDTH;
    }

    public int getSolidTop() {

        return y + SOLID_OFFSET_Y;
    }

    public int getSolidBottom() {

        return getSolidTop() + SOLID_HEIGHT;
    }
}

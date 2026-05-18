package fantasyrpg.ui.world;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

public class Player {

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
                x + 8,
                y + 16,
                40,
                40
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
                nextX + 8,
                nextY + 16,
                40,
                40
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
                64,
                64,
                null
        );
    }
}

package fantasyrpg.ui.world;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    
    int speed = 2;

    BufferedImage front;
    BufferedImage back;
    BufferedImage left;
    BufferedImage right;

    BufferedImage currentSprite;

    Rectangle solidArea;

    
    private double bobCounter = 0;
    private final List<DustParticle> dustParticles = new ArrayList<>();

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

    
    public int getDrawWidth() {
        if (currentSprite == null) {
            return DRAW_SIZE;
        }
        return (int) (DRAW_SIZE * ((double) currentSprite.getWidth() / currentSprite.getHeight()));
    }

    public int getDrawHeight() {
        return DRAW_SIZE;
    }

    public void update() {

        int nextX = x;
        int nextY = y;

        boolean moveUp = false;
        boolean moveDown = false;
        boolean moveLeft = false;
        boolean moveRight = false;

        
        if (keyInput.leftPressed && keyInput.rightPressed) {
            if (keyInput.lastHorizontal == 'L') {
                moveLeft = true;
            } else if (keyInput.lastHorizontal == 'R') {
                moveRight = true;
            }
        } else {
            if (keyInput.leftPressed) {
                moveLeft = true;
            }
            if (keyInput.rightPressed) {
                moveRight = true;
            }
        }

        
        if (keyInput.upPressed && keyInput.downPressed) {
            if (keyInput.lastVertical == 'U') {
                moveUp = true;
            } else if (keyInput.lastVertical == 'D') {
                moveDown = true;
            }
        } else {
            if (keyInput.upPressed) {
                moveUp = true;
            }
            if (keyInput.downPressed) {
                moveDown = true;
            }
        }

        if (moveUp) {
            nextY -= speed;
            currentSprite = back;
        }
        if (moveDown) {
            nextY += speed;
            currentSprite = front;
        }
        if (moveLeft) {
            nextX -= speed;
            currentSprite = left;
        }
        if (moveRight) {
            nextX += speed;
            currentSprite = right;
        }

        boolean isMoving = moveUp || moveDown || moveLeft || moveRight;

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
            solidArea.setLocation(
                    x + SOLID_OFFSET_X,
                    y + SOLID_OFFSET_Y
            );
        }

        
        if (isMoving && !collision) {
            bobCounter += 0.22;

            int drawW = getDrawWidth();
            int drawH = getDrawHeight();
            int drawX = x - (drawW - DRAW_SIZE) / 2;
            int drawY = y;

            int feetX = drawX + drawW / 2;
            int feetY = drawY + drawH - 14; 

            
            if (Math.random() < 0.40) {
                dustParticles.add(new DustParticle(feetX, feetY));
            }
        } else {
            bobCounter = 0;
        }

        
        for (int i = dustParticles.size() - 1; i >= 0; i--) {
            DustParticle p = dustParticles.get(i);
            p.update();
            if (p.isDead()) {
                dustParticles.remove(i);
            }
        }
    }

    public void draw(Graphics2D g2) {
        int drawW = getDrawWidth();
        int drawH = getDrawHeight();

        
        int drawX = x - (drawW - DRAW_SIZE) / 2;
        int drawY = y;

        
        for (DustParticle p : dustParticles) {
            p.draw(g2);
        }

        boolean isMoving = keyInput.upPressed || keyInput.downPressed || keyInput.leftPressed || keyInput.rightPressed;

        
        if (isMoving) {
            Graphics2D g2d = (Graphics2D) g2.create();
            
            g2d.translate(drawX + drawW / 2, drawY + drawH);

            
            double angle = Math.sin(bobCounter) * 0.08;
            g2d.rotate(angle);

            
            double squash = 1.0 + Math.abs(Math.sin(bobCounter)) * 0.05;
            double stretch = 1.0 - Math.abs(Math.sin(bobCounter)) * 0.03;
            g2d.scale(stretch, squash);

            
            g2d.drawImage(
                    currentSprite,
                    -drawW / 2,
                    -drawH,
                    drawW,
                    drawH,
                    null
            );
            g2d.dispose();
        } else {
            
            g2.drawImage(
                    currentSprite,
                    drawX,
                    drawY,
                    drawW,
                    drawH,
                    null
            );
        }
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

    
    private static class DustParticle {
        double x, y;
        double vx, vy;
        double size;
        double alpha;

        public DustParticle(double x, double y) {
            this.x = x;
            this.y = y;
            
            this.vx = (Math.random() - 0.5) * 1.0;
            this.vy = -Math.random() * 0.5 - 0.2; 
            this.size = Math.random() * 7 + 4; 
            this.alpha = 0.85; 
        }

        public void update() {
            x += vx;
            y += vy;
            alpha -= 0.018; 
            if (size > 1) {
                size -= 0.08;
            }
        }

        public boolean isDead() {
            return alpha <= 0 || size <= 1;
        }

        public void draw(Graphics2D g2) {
            g2.setColor(new Color(220, 215, 205, (int) (alpha * 255)));
            g2.fillOval((int) (x - size / 2), (int) (y - size / 2), (int) size, (int) size);
        }
    }
}

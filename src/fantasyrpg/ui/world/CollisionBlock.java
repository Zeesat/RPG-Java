package fantasyrpg.ui.world;

import java.awt.Rectangle;

public class CollisionBlock {

    Rectangle rectangle;

    public CollisionBlock(
            int x,
            int y,
            int width,
            int height
    ) {

        rectangle = new Rectangle(
                x,
                y,
                width,
                height
        );
    }
}
package fantasyrpg;

public class GameState {
    
    public static String currentMapPath = "assets/maps/maps.tmx";
    public static int playerX = -1;
    public static int playerY = -1;
    
    
    public static boolean map1Enemy1Defeated = false;
    public static boolean map1Enemy2Defeated = false;

    
    public static boolean map2Enemy1Defeated = false;
    public static boolean map2Enemy2Defeated = false;
    public static boolean map2Enemy3Defeated = false;

    
    public static int map1Enemy1X = -1;
    public static int map1Enemy1Y = -1;
    public static int map1Enemy2X = -1;
    public static int map1Enemy2Y = -1;

    
    public static int currentEnemyIndex = -1; 
    
    public static void reset() {
        currentMapPath = "assets/maps/maps.tmx";
        playerX = -1;
        playerY = -1;
        map1Enemy1Defeated = false;
        map1Enemy2Defeated = false;
        map2Enemy1Defeated = false;
        map2Enemy2Defeated = false;
        map2Enemy3Defeated = false;
        map1Enemy1X = -1;
        map1Enemy1Y = -1;
        map1Enemy2X = -1;
        map1Enemy2Y = -1;
        currentEnemyIndex = -1;
    }
}

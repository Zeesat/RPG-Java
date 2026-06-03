package fantasyrpg;

public class GameState {
    // Current world state
    public static String currentMapPath = "assets/maps/maps.tmx";
    public static int playerX = -1;
    public static int playerY = -1;
    
    // Defeated states
    public static boolean map1Enemy1Defeated = false;
    public static boolean map1Enemy2Defeated = false;
    public static boolean map2Enemy1Defeated = false;
    public static boolean map2Enemy2Defeated = false;

    // Positions of active enemies on Map 1 (to keep them persistent)
    public static int map1Enemy1X = -1;
    public static int map1Enemy1Y = -1;
    public static int map1Enemy2X = -1;
    public static int map1Enemy2Y = -1;

    // Which enemy index is currently being fought
    public static int currentEnemyIndex = -1; 
    
    public static void reset() {
        currentMapPath = "assets/maps/maps.tmx";
        playerX = -1;
        playerY = -1;
        map1Enemy1Defeated = false;
        map1Enemy2Defeated = false;
        map2Enemy1Defeated = false;
        map2Enemy2Defeated = false;
        map1Enemy1X = -1;
        map1Enemy1Y = -1;
        map1Enemy2X = -1;
        map1Enemy2Y = -1;
        currentEnemyIndex = -1;
    }
}

package fantasyrpg.ui.world;

import org.w3c.dom.*;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;

public class TiledMapLoader {

    public int[][] mapData;
    public ArrayList<int[][]> mapLayers =
            new ArrayList<>();
    public ArrayList<String> mapLayerNames =
            new ArrayList<>();

    public int mapWidth;
    public int mapHeight;

    public int tileWidth;
    public int tileHeight;

    public int spawnX;
    public int spawnY;
    public ArrayList<EnemySpawnPoint> enemySpawnPoints =
            new ArrayList<>();

    // TILE IMAGE
    public HashMap<Integer, BufferedImage> tiles =
            new HashMap<>();

    // COLLISION
    public ArrayList<Rectangle> collisions =
            new ArrayList<>();

    public TiledMapLoader(String mapPath) {

        loadMap(mapPath);
    }

    private void loadMap(String path) {

        try {

            File file = new File(path);

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            Document document =
                    builder.parse(file);

            document.getDocumentElement().normalize();

            Element mapElement =
                    document.getDocumentElement();

            mapWidth =
                    Integer.parseInt(
                            mapElement.getAttribute("width")
                    );

            mapHeight =
                    Integer.parseInt(
                            mapElement.getAttribute("height")
                    );

            tileWidth =
                    Integer.parseInt(
                            mapElement.getAttribute("tilewidth")
                    );

            tileHeight =
                    Integer.parseInt(
                            mapElement.getAttribute("tileheight")
                    );

            spawnX =
                    tileWidth;

            spawnY =
                    tileHeight;

            enemySpawnPoints.clear();

            // =========================
            // LOAD ALL TSX TILESETS
            // =========================

            NodeList tilesetList =
                    document.getElementsByTagName("tileset");

            for (int i = 0; i < tilesetList.getLength(); i++) {
                Element tilesetElement = (Element) tilesetList.item(i);
                String tsxSource = tilesetElement.getAttribute("source");
                int firstgid = 1;
                String firstgidAttr = tilesetElement.getAttribute("firstgid");
                if (firstgidAttr != null && !firstgidAttr.isEmpty()) {
                    firstgid = Integer.parseInt(firstgidAttr);
                }

                if (tsxSource != null && !tsxSource.isEmpty()) {
                    File tsxFile;
                    if (tsxSource.contains(":") || tsxSource.startsWith("/")) {
                        tsxFile = new File(tsxSource);
                    } else {
                        tsxFile = new File(file.getParentFile(), tsxSource);
                    }
                    loadTSX(tsxFile, firstgid);
                }
            }

            // =========================
            // LOAD TILE LAYER
            // =========================

            NodeList layerList =
                    document.getElementsByTagName("layer");

            for (int layerIndex = 0;
                 layerIndex < layerList.getLength();
                 layerIndex++) {

                Element layer =
                        (Element) layerList.item(layerIndex);

                String layerName =
                        layer.getAttribute("name");

                String csvData =
                        layer
                                .getElementsByTagName("data")
                                .item(0)
                                .getTextContent()
                                .trim();

                String[] numbers =
                        csvData.split(",");

                int[][] layerData =
                        new int[mapHeight][mapWidth];

                int index = 0;

                for (int row = 0; row < mapHeight; row++) {

                    for (int col = 0; col < mapWidth; col++) {

                        layerData[row][col] =
                                Integer.parseInt(
                                        numbers[index].trim()
                                );

                        index++;
                    }
                }

                mapLayers.add(layerData);
                mapLayerNames.add(layerName);
            }

            mapData =
                    mapLayers.isEmpty()
                            ? new int[mapHeight][mapWidth]
                            : mapLayers.get(0);

            // =========================
            // LOAD COLLISION
            // =========================

            NodeList objectGroups =
                    document.getElementsByTagName(
                            "objectgroup"
                    );

            for (int i = 0; i < objectGroups.getLength(); i++) {

                Element objectGroup =
                        (Element) objectGroups.item(i);

                String name =
                        objectGroup.getAttribute("name");

                if (name.equalsIgnoreCase("collison")
                        || name.equalsIgnoreCase("collision")) {

                    loadCollisionObjects(objectGroup);
                }

                if (name.equalsIgnoreCase("Spawn")) {

                    loadSpawnPoint(objectGroup);
                }

                if (isEnemySpawnGroupName(name)) {

                    loadEnemySpawnPoints(objectGroup);
                }
            }

            System.out.println("MAP SUCCESS");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =========================
    // LOAD TSX
    // =========================

    private void loadTSX(File tsxFile, int firstgid) {

        try {

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            Document document;
            if (tsxFile.exists()) {
                document = builder.parse(tsxFile);
            } else {
                // Fallback: look for the TSX file in local project directories if path doesn't exist
                String tsxName = tsxFile.getName();
                File localTSX = new File("assets/tiles", tsxName);
                if (!localTSX.exists()) {
                    localTSX = new File("assets/maps", tsxName);
                }
                if (localTSX.exists()) {
                    document = builder.parse(localTSX);
                } else {
                    System.err.println("TSX file not found: " + tsxFile.getPath());
                    return;
                }
            }

            document.getDocumentElement().normalize();

            NodeList tileList =
                    document.getElementsByTagName("tile");

            for (int i = 0; i < tileList.getLength(); i++) {

                Element tileElement =
                        (Element) tileList.item(i);

                int id =
                        Integer.parseInt(
                                tileElement.getAttribute("id")
                        ) + firstgid;

                Element imageElement =
                        (Element)
                                tileElement
                                        .getElementsByTagName("image")
                                        .item(0);

                String source =
                        imageElement.getAttribute("source");

                // Dynamically resolve filename from the source attribute to assets/tiles/
                String fileName = new File(source).getName();
                File imageFile = new File("assets/tiles", fileName);

                BufferedImage image =
                        ImageIO.read(imageFile);

                tiles.put(id, image);

                System.out.println(
                        "Loaded Tile: " + fileName + " as GID " + id
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void loadCollisionObjects(Element objectGroup) {

        NodeList objects =
                objectGroup.getElementsByTagName(
                        "object"
                );

        for (int j = 0; j < objects.getLength(); j++) {

            Element object =
                    (Element) objects.item(j);

            int x =
                    readIntAttribute(object, "x", 0);

            int y =
                    readIntAttribute(object, "y", 0);

            int width =
                    readIntAttribute(object, "width", 0);

            int height =
                    readIntAttribute(object, "height", 0);

            if (width > 0 && height > 0) {

                collisions.add(
                        new Rectangle(
                                x,
                                y,
                                width,
                                height
                        )
                );
            }
        }
    }

    private void loadSpawnPoint(Element objectGroup) {

        NodeList objects =
                objectGroup.getElementsByTagName(
                        "object"
                );

        if (objects.getLength() == 0) {
            return;
        }

        Element object =
                (Element) objects.item(0);

        spawnX =
                readIntAttribute(object, "x", spawnX);

        spawnY =
                readIntAttribute(object, "y", spawnY);
    }

    private boolean isEnemySpawnGroupName(String objectGroupName) {

        if (objectGroupName == null) {
            return false;
        }

        String normalizedName =
                objectGroupName
                        .trim()
                        .toLowerCase()
                        .replace(" ", "")
                        .replace("_", "")
                        .replace("-", "");

        return normalizedName.equals("enemyspawns")
                || normalizedName.equals("enemyspawn");
    }

    private void loadEnemySpawnPoints(Element objectGroup) {

        NodeList objects =
                objectGroup.getElementsByTagName(
                        "object"
                );

        for (int j = 0; j < objects.getLength(); j++) {

            Element object =
                    (Element) objects.item(j);

            int x =
                    readIntAttribute(object, "x", 0);

            int y =
                    readIntAttribute(object, "y", 0);

            int width =
                    readIntAttribute(object, "width", 0);

            int height =
                    readIntAttribute(object, "height", 0);

            // If designer uses rectangle objects, treat center as spawn anchor.
            if (width > 0 || height > 0) {
                x += width / 2;
                y += height / 2;
            }

            String enemyId =
                    readEnemyId(object);

            enemySpawnPoints.add(
                    new EnemySpawnPoint(
                            x,
                            y,
                            enemyId
                    )
            );
        }
    }

    private String readEnemyId(Element object) {

        String enemyId =
                readObjectProperty(
                        object,
                        "enemyId"
                );

        if (enemyId == null || enemyId.isBlank()) {
            enemyId =
                    readObjectProperty(
                            object,
                            "enemy_id"
                    );
        }

        if (enemyId == null || enemyId.isBlank()) {
            enemyId =
                    object.getAttribute("name");
        }

        if (enemyId == null || enemyId.isBlank()) {
            return "default";
        }

        return enemyId.trim();
    }

    private String readObjectProperty(
            Element object,
            String propertyName
    ) {

        NodeList propertiesList =
                object.getElementsByTagName(
                        "properties"
                );

        if (propertiesList.getLength() == 0) {
            return "";
        }

        Element propertiesElement =
                (Element) propertiesList.item(0);

        NodeList propertyList =
                propertiesElement.getElementsByTagName(
                        "property"
                );

        for (int i = 0; i < propertyList.getLength(); i++) {

            Element property =
                    (Element) propertyList.item(i);

            String name =
                    property.getAttribute("name");

            if (!propertyName.equals(name)) {
                continue;
            }

            String value =
                    property.getAttribute("value");

            if (value != null && !value.isBlank()) {
                return value;
            }

            return property.getTextContent();
        }

        return "";
    }

    private int readIntAttribute(
            Element element,
            String attributeName,
            int defaultValue
    ) {

        String value =
                element.getAttribute(attributeName);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return (int) Float.parseFloat(value);
    }
}

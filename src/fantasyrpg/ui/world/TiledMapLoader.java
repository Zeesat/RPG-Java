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

            // =========================
            // LOAD TSX
            // =========================

            NodeList tilesetList =
                    document.getElementsByTagName("tileset");

            Element tilesetElement =
                    (Element) tilesetList.item(0);

            String tsxSource =
                    tilesetElement.getAttribute("source");

            File tsxFile =
                    new File(
                            file.getParentFile(),
                            tsxSource
                    );

            loadTSX(tsxFile);

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
            }

            System.out.println("MAP SUCCESS");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =========================
    // LOAD TSX
    // =========================

    private void loadTSX(File tsxFile) {

        try {

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            Document document =
                    builder.parse(tsxFile);

            document.getDocumentElement().normalize();

            NodeList tileList =
                    document.getElementsByTagName("tile");

            for (int i = 0; i < tileList.getLength(); i++) {

                Element tileElement =
                        (Element) tileList.item(i);

                int id =
                        Integer.parseInt(
                                tileElement.getAttribute("id")
                        ) + 1;

                Element imageElement =
                        (Element)
                                tileElement
                                        .getElementsByTagName("image")
                                        .item(0);

                String source =
                        imageElement.getAttribute("source");

                File imageFile =
                        new File(
                                tsxFile.getParentFile(),
                                source
                        );

                BufferedImage image =
                        ImageIO.read(imageFile);

                tiles.put(id, image);

                System.out.println(
                        "Loaded Tile: " + source
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

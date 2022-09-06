package de.fhac.mazenet.server.userinterface.betterUI;

import javax.imageio.ImageIO;

import de.fhac.mazenet.server.config.UISettings;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class ImageResources {
    private static HashMap<String, Image> images = new HashMap<String, Image>();

    private ImageResources() {
    }

    public static Image getImage(String name) {
        if (images.containsKey(name)) {
            return images.get(name);
        }
        URL url = ImageResources.class.getResource(UISettings.IMAGEPATH + name + UISettings.IMAGEFILEEXTENSION);
        Image img = null;
        try {
            img = ImageIO.read(url);
            images.put(name, img);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    public static void reset() {
        images = new HashMap<String, Image>();
    }

    public static void treasureFound(String treasure) {
        if (!treasure.startsWith("Start")) {
            URL url = ImageResources.class.getResource(UISettings.IMAGEPATH + "found" + UISettings.IMAGEFILEEXTENSION);
            try {
                images.put(treasure, ImageIO.read(url));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
package de.fhac.mazenet.server.userinterface.mazeFX.objects;

import de.fhac.mazenet.server.config.UISettings;
import de.fhac.mazenet.server.game.Card;
import de.fhac.mazenet.server.generated.CardData;
import de.fhac.mazenet.server.generated.Treasure;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;

import java.util.HashMap;

/**
 * Created by Richard Zameitat on 26.05.2016.
 */
public class CardFX extends Box {

    private static final double TREASURE_OFFSET_Y = -0.1;
    private static final HashMap<Card.CardShape, Image> CARD_SHAPE_IMAGE_MAPPING;
    private final static double SIZE_X = 1, SIZE_Y = 0.1, SIZE_Z = 1;

    static {
        final String imgPre = UISettings.IMAGEPATH;
        CARD_SHAPE_IMAGE_MAPPING = new HashMap<>();
        CARD_SHAPE_IMAGE_MAPPING.put(Card.CardShape.I, new Image(imgPre + "I0.png"));
        CARD_SHAPE_IMAGE_MAPPING.put(Card.CardShape.L, new Image(imgPre + "L0.png"));
        CARD_SHAPE_IMAGE_MAPPING.put(Card.CardShape.T, new Image(imgPre + "T0.png"));
    }

    // private Box box;
    private TreasureFX treasure = null;
    private PhongMaterial material;

    private CardFX() {
        super(SIZE_X, SIZE_Y, SIZE_Z);
        setDrawMode(DrawMode.FILL);
        setCullFace(CullFace.NONE);
        material = new PhongMaterial(Color.WHITE);
        setMaterial(material);
        setRotationAxis(new Point3D(0, 1, 0));
    }

    public CardFX(CardData card, Group root3d) {
        this();
        // um card methoden zu nutzen...
        Card tempCard = new Card(card);
        Card.CardShape cardShape = tempCard.getShape();
        material.setDiffuseMap(CARD_SHAPE_IMAGE_MAPPING.get(cardShape));
        switch (tempCard.getOrientation()) {
        case D0:
            break;
        case D90:
            setRotate(90);
            break;
        case D180:
            setRotate(180);
            break;
        case D270:
            setRotate(270);
            break;
        }
        Treasure treasure = card.getTreasure();
        if (treasure != null) {
            this.treasure = new TreasureFX(treasure);
            this.treasure.translateXProperty().bind(translateXProperty());
            this.treasure.translateYProperty().bind(Bindings.add(TREASURE_OFFSET_Y, translateYProperty()));
            this.treasure.translateZProperty().bind(translateZProperty());
            root3d.getChildren().add(this.treasure);
        }
    }

    public void removeFrom(Group root3d) {
        root3d.getChildren().removeAll(this, treasure);
    }

    public TreasureFX getTreasure() {
        return treasure;
    }

    /*
     * public void updateFromCardType(CardData ct){ Card tC = new Card(ct);
     * Card.CardShape cS = tC.getShape();
     * material.setDiffuseMap(CARD_SHAPE_IMAGE_MAPPING.get(cS)); switch
     * (tC.getOrientation()){ case D0: break; case D90: setRotate(90); break;
     * case D180: setRotate(180); break; case D270: setRotate(270); break; } }/
     **/
}
